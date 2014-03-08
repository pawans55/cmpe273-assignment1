package edu.sjsu.cmpe.library.api.resources;

import java.util.HashMap;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.NotFoundException;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;
import edu.sjsu.cmpe.library.dto.AuthorDto;
import edu.sjsu.cmpe.library.dto.AuthorsDto;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.LinksDto;
import edu.sjsu.cmpe.library.dto.ReviewDto;
import edu.sjsu.cmpe.library.dto.ReviewsDto;

/**
 * This is Main BookResource class containing all library APIs 
 */

@Path("/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

	private static long book_id=1;
	private static long author_id=1;
	private static long review_id=1;
	private static HashMap<Long, Book> new_book_entry = new HashMap<Long,Book>();

	/**
	 * API number 2: It should create and add new book in library with expected 201 HTTP code.
	 */
	@POST
	@Timed(name = "create-book")
	public Response createBook(@Valid Book book) {
		book.setIsbn(book_id); 
		new_book_entry.put(book_id, book);
		book_id++;

		for (int author=0;author<book.getAuthors().length;author++)
		{
			book.getAuthors()[author].id=author_id;
			author_id++;

		}

		BookDto bookObject = new BookDto();
		bookObject.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(), "GET"));
		bookObject.addLink(new LinkDto("update-book","/books/" + book.getIsbn(), "PUT"));
		bookObject.addLink(new LinkDto("delete-book","/books/" + book.getIsbn(), "DELETE"));
		bookObject.addLink(new LinkDto("create-review","/books/" + book.getIsbn() + "/reviews", "POST"));

		return Response.status(201).entity(bookObject.getLinks()).build();
	}
    
	/**
	 * API number 3: It should view books based on ISBN number.
	 */
	@GET
    @Path("/{isbn}")
    @Timed(name = "view-book")
    public BookDto viewBook(@PathParam("isbn") long isbn) {

		Book retrieveBook=new_book_entry.get(isbn);

		BookDto bookObject = new BookDto(retrieveBook);
		bookObject.addLink(new LinkDto("view-book", "/books/" + retrieveBook.getIsbn(), "GET"));
		bookObject.addLink(new LinkDto("update-book","/books/" + retrieveBook.getIsbn(), "PUT"));
		bookObject.addLink(new LinkDto("delete-book","/books/" + retrieveBook.getIsbn(), "DELETE"));
		bookObject.addLink(new LinkDto("create-review","/books/" + retrieveBook.getIsbn() + "/reviews", "POST"));
		if (retrieveBook.getReviews().size() !=0 ){
		bookObject.addLink(new LinkDto("view-all-reviews","/books/" + retrieveBook.getIsbn() + "/reviews", "GET"));
		}

	return bookObject;
    }

	/**
	 * API number 4: It should delete book from the library based on the ISBN number
	 */
	@DELETE
    @Path("/{isbn}")
    @Timed(name = "delete-book")
    public Response deleteBook(@PathParam("isbn") long isbn) {

		new_book_entry.remove(isbn);

		LinksDto links = new LinksDto();
		links.addLink(new LinkDto("create-book", "/books", "POST"));

	return Response.ok(links).build();
    }

	/**
	 * API number 5: It should update the book status
	 * @throws Exception 
	 */
	@PUT
    @Path("/{isbn}")
    @Timed(name = "update-book")
    public Response updateBook(@PathParam("isbn") long isbn, @QueryParam("status") String status) throws Exception {

		try{
			if(!status.equalsIgnoreCase("avialable") &&
				!status.equalsIgnoreCase("lost") &&
				!status.equalsIgnoreCase("checked-out") &&
				!status.equalsIgnoreCase("in-queue")) {
			throw new NotFoundException(" Invalidvalues cant be accepted Valid values are only avialable,lost,checked-out,in-queue");

			}
		}	catch (Exception e) {
			throw e;
		}

		Book retrieveBook=new_book_entry.get(isbn);
		retrieveBook.setStatus(status);

		BookDto bookObject = new BookDto();
		bookObject.addLink(new LinkDto("view-book", "/books/" + retrieveBook.getIsbn(), "GET"));
		bookObject.addLink(new LinkDto("update-book","/books/" + retrieveBook.getIsbn(), "PUT"));
		bookObject.addLink(new LinkDto("delete-book","/books/" + retrieveBook.getIsbn(), "DELETE"));
		bookObject.addLink(new LinkDto("create-review","/books/" + retrieveBook.getIsbn() + "/reviews", "POST"));
		if (retrieveBook.getReviews().size() !=0 ){
			bookObject.addLink(new LinkDto("view-all-reviews","/books/" + retrieveBook.getIsbn() + "/reviews", "GET"));
			}

	return Response.ok().entity(bookObject.getLinks()).build();
    }

	/**
	 * API number 6: It should create new reviews for the books in library with expected 201 HTTP code.
	 */
	@POST
    @Path("/{isbn}/reviews")
    @Timed(name = "create-review")
    public Response createReview(@Valid Review reviews, @PathParam("isbn") long isbn) {

		Book retrieveBook = new_book_entry.get(isbn);

		reviews.setID(review_id);
		retrieveBook.getReviews().add(reviews);
		review_id++;

		ReviewDto reviewobject = new ReviewDto();
		reviewobject.addLink(new LinkDto("view-review", "/books/" + retrieveBook.getIsbn() + "/reviews/" + reviews.getID(), "GET"));

	return Response.status(201).entity(reviewobject.getLinks()).build();
    }

	/**
	 * API number 7: It should view reviews of the book with expected 201 HTTP code.
	 */
	@GET
    @Path("/{isbn}/reviews/{id}")
    @Timed(name = "view-review")
    public ReviewDto viewReview(@PathParam("isbn") long isbn, @PathParam("id") long id) {
		int i=0;
		Book retrieveBook = new_book_entry.get(isbn);

		while (retrieveBook.getoneReview(i).getID()!=id)
		{
			i++;
		}

		ReviewDto reviewobject = new ReviewDto(retrieveBook.getoneReview(i));
		reviewobject.addLink(new LinkDto("view-review", "/books/" + retrieveBook.getIsbn() + "/reviews/" + retrieveBook.getoneReview(i).getID(), "GET"));

	return reviewobject;
    }

	/**
	 * API number 8: It should view all reviews of a book in  library based on its ISBN number.
	 */
	@GET
    @Path("/{isbn}/reviews")
    @Timed(name = "view-all-reviews")
    public ReviewsDto viewAllReviews(@PathParam("isbn") long isbn) {

		Book retrieveBook = new_book_entry.get(isbn);
		ReviewsDto reviewobject = new ReviewsDto(retrieveBook.getReviews());

	return reviewobject;
    }

	/**
	 * API number 9: It should view author based on authors ID
	 */
	@GET
    @Path("/{isbn}/authors/{id}")
    @Timed(name = "view-author")
    public Response viewAuthor(@PathParam("isbn") long isbn, @PathParam("id") long id) {
		int i=0;
		Book retrieveBook = new_book_entry.get(isbn);
		while (retrieveBook.getoneAuthors((int)i).id!=id)
		{
			i++;
		}
		AuthorDto authorResponse = new AuthorDto(retrieveBook.getoneAuthors((int)i));
		authorResponse.addLink(new LinkDto("view-author", "/books/" + retrieveBook.getIsbn() + "/authors/" + retrieveBook.getoneAuthors((int)i).id, "GET"));

	return Response.ok(authorResponse).build();
    }

	/**
	 * API number 10: It should view all authors of a book based on ISBN of book
	 */
	@GET
    @Path("/{isbn}/authors")
    @Timed(name = "view-all-authors")
    public AuthorsDto viewAllAuthors(@PathParam("isbn") long isbn) {

		Book retrieveBook = new_book_entry.get(isbn);
		AuthorsDto authorResponse = new AuthorsDto(retrieveBook.getAuthors());

	return authorResponse;
    }
}
