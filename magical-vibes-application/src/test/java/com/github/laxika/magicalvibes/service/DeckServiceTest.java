package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.entity.Deck;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.networking.message.SaveDeckRequest;
import com.github.laxika.magicalvibes.repository.DeckRepository;
import com.github.laxika.magicalvibes.webservice.DeckService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckServiceTest {
    private final DeckRepository repository = mock(DeckRepository.class);
    private final DeckValidationService validator = mock(DeckValidationService.class);
    private final DeckService service = new DeckService(repository, new ObjectMapper(), mock(CardCatalog.class), validator);

    @Test void savesInvalidDraftAndEditsSameOwnedDeck() {
        UUID user = UUID.randomUUID();
        var invalid = new DeckValidation(List.of("Select one commander"), null);
        when(validator.validate(any(), any())).thenReturn(invalid);
        when(repository.save(any())).thenAnswer(invocation -> {
            Deck saved = invocation.getArgument(0);
            when(repository.findById(saved.getId())).thenReturn(Optional.of(saved));
            return saved;
        });
        var saved = service.saveDeck(user, new SaveDeckRequest("Draft", List.of(), null, DeckFormat.COMMANDER, List.of(), null));
        assertThat(saved.deck().validation().valid()).isFalse();
        var loaded = service.load(user, saved.deck().id());
        assertThat(loaded.name()).isEqualTo("Draft");
        assertThat(loaded.format()).isEqualTo(DeckFormat.COMMANDER);
        var edited = service.saveDeck(user, new SaveDeckRequest("Renamed", List.of(), loaded.id(), DeckFormat.MODERN, List.of(), null));
        assertThat(edited.deck().id()).isEqualTo(saved.deck().id());
        assertThat(service.load(user, loaded.id()).name()).isEqualTo("Renamed");
    }

    @Test void cannotLoadOrOverwriteAnotherUsersDeck() {
        Deck deck = new Deck(UUID.randomUUID(), "Private", "[]");
        when(repository.findById(deck.getId())).thenReturn(Optional.of(deck));
        UUID outsider = UUID.randomUUID();
        String id = "custom-" + deck.getId();
        when(validator.validate(any(), any())).thenReturn(new DeckValidation(List.of(), null));
        assertThatThrownBy(() -> service.load(outsider, id)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.saveDeck(outsider, new SaveDeckRequest("Overwrite", List.of(), id, DeckFormat.CASUAL, List.of(), null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test void legacyDeckDefaultsToCasualWithoutSideboardOrCommander() {
        Deck deck = new Deck(UUID.randomUUID(), "Old deck", "[]"); deck.setSideboardJson(null);
        when(repository.findById(deck.getId())).thenReturn(Optional.of(deck));
        var loaded = service.load(deck.getUserId(), "custom-" + deck.getId());
        assertThat(loaded.format()).isEqualTo(DeckFormat.CASUAL);
        assertThat(loaded.sideboard()).isEmpty();
        assertThat(loaded.commander()).isNull();
        var legacyRequest = new ObjectMapper().readValue("{\"name\":\"Old request\",\"entries\":[]}", SaveDeckRequest.class);
        assertThat(legacyRequest.format()).isEqualTo(DeckFormat.CASUAL);
    }
}
