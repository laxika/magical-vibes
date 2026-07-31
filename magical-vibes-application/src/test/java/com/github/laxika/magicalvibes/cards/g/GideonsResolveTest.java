package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GideonsResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gideon's Resolve triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // resolve enchantment -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Gideon, Martial Paragon in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card gideon = createGideonMartialParagon();
        harness.setGraveyard(player1, List.of(gideon));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Gideon, Martial Paragon");
        harness.assertNotInGraveyard(player1, "Gideon, Martial Paragon");
    }

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card gideon = createGideonMartialParagon();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(gideon);
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst()
                .getName()).isEqualTo("Gideon, Martial Paragon");
    }

    @Test
    @DisplayName("Accepting may when Gideon is not in library or graveyard does nothing")
    void acceptingMayWhenNotFound() {
        gd.playerDecks.get(player1.getId()).clear();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may ability does not search")
    void decliningMayDoesNotSearch() {
        Card gideon = createGideonMartialParagon();
        harness.setGraveyard(player1, List.of(gideon));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Gideon, Martial Paragon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gideon's Resolve enters the battlefield after resolving")
    void entersBattlefield() {
        setupAndCast();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gideon's Resolve");
    }

    @Test
    @DisplayName("Own creatures get +1/+1")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new GideonsResolve());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");

        // Elite Vanguard is 2/1, with +1/+1 should be 3/2
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures do not get buffed")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new GideonsResolve());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent opponentVanguard = findPermanent(player2, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(1);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GideonsResolve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
    }

    private Card createGideonMartialParagon() {
        Card gideon = new Card();
        gideon.setName("Gideon, Martial Paragon");
        gideon.setType(CardType.PLANESWALKER);
        gideon.setManaCost("{4}{W}");
        return gideon;
    }
}
