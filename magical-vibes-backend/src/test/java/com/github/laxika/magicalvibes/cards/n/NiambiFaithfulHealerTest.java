package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NiambiFaithfulHealerTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Has MayEffect wrapping SearchLibraryAndOrGraveyardForNamedCardToHandEffect on ETB")
    void hasCorrectEffects() {
        NiambiFaithfulHealer card = new NiambiFaithfulHealer();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SearchLibraryAndOrGraveyardForNamedCardToHandEffect.class);

        SearchLibraryAndOrGraveyardForNamedCardToHandEffect searchEffect =
                (SearchLibraryAndOrGraveyardForNamedCardToHandEffect) mayEffect.wrapped();
        assertThat(searchEffect.cardName()).isEqualTo("Teferi, Timebender");
    }

    // ===== ETB triggers may prompt =====

    @Test
    @DisplayName("Resolving Niambi triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept may: finds in graveyard =====

    @Test
    @DisplayName("Accepting may finds Teferi, Timebender in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card teferi = createTeferiTimebender();
        harness.setGraveyard(player1, List.of(teferi));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Teferi, Timebender"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Teferi, Timebender"));
    }

    // ===== Accept may: finds in library =====

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card teferi = createTeferiTimebender();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(teferi);
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        // Library search prompt should appear
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Teferi, Timebender");
    }

    // ===== Accept may: not found anywhere =====

    @Test
    @DisplayName("Accepting may when Teferi is not in library or graveyard does nothing")
    void acceptingMayWhenNotFound() {
        gd.playerDecks.get(player1.getId()).clear();
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Decline may =====

    @Test
    @DisplayName("Declining may ability does not search")
    void decliningMayDoesNotSearch() {
        Card teferi = createTeferiTimebender();
        harness.setGraveyard(player1, List.of(teferi));
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        // Teferi stays in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Teferi, Timebender"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Niambi enters battlefield =====

    @Test
    @DisplayName("Niambi enters the battlefield after resolving")
    void niambiEntersBattlefield() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Niambi, Faithful Healer"));
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new NiambiFaithfulHealer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }

    private Card createTeferiTimebender() {
        Card teferi = new Card();
        teferi.setName("Teferi, Timebender");
        teferi.setType(CardType.PLANESWALKER);
        teferi.setManaCost("{4}{W}{U}");
        return teferi;
    }
}
