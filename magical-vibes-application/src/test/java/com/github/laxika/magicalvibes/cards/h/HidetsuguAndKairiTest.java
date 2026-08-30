package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HidetsuguAndKairi.class, GrizzlyBears.class, Shock.class})
class HidetsuguAndKairiTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing three cards and putting two chosen cards on top")
    void entersDrawsThreeAndPutsTwoOnTop() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        Card fourth = new Shock();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new HidetsuguAndKairi()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, fourth);
    }

    @Test
    @DisplayName("Death trigger exiles the top card and makes the chosen opponent lose its mana value")
    void deathTriggerExilesTopCardAndLosesManaValue() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        kill(source);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Death trigger offers an instant or sorcery for a free cast")
    void deathTriggerOffersInstantOrSorceryForFreeCast() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        Card topCard = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        kill(source);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Death trigger does not offer a non-instant or non-sorcery card")
    void deathTriggerDoesNotOfferCreatureForFreeCast() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        kill(source);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private Permanent addHidetsuguAndKairiWithResolvedEtb() {
        harness.setLibrary(player1, List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HidetsuguAndKairi());
        harness.passBothPriorities();
        return source;
    }

    private void kill(Permanent source) {
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
