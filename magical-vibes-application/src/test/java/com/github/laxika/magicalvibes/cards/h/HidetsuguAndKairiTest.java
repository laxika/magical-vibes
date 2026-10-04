package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HidetsuguAndKairi.class, GrizzlyBears.class, Shock.class, LavaAxe.class, TrueBeliever.class})
class HidetsuguAndKairiTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing three cards and putting two chosen cards on top")
    void entersDrawsThreeAndPutsTwoOnTop() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        Card fourth = new Shock();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.castFromHand(player1, new HidetsuguAndKairi(), "{2}{U}{U}{B}");
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

    @Test
    @DisplayName("Death trigger may be declined while the opponent still loses life")
    void decliningCastLeavesInstantExiled() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        kill(source);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger does nothing with an empty library")
    void emptyLibraryDoesNotLoseLifeOrOfferCast() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        kill(source);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger casts a sorcery without mana and uses its full mana value")
    void deathTriggerCastsSorceryWithoutMana() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        Card topCard = new LavaAxe();
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        kill(source);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 10);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Enter trigger can put preexisting hand cards on top in the chosen order")
    void enterTriggerCanReturnPreexistingHandCardsInReverseOrder() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card drawnFirst = new GrizzlyBears();
        Card drawnSecond = new Shock();
        Card drawnThird = new GrizzlyBears();
        Card remaining = new Shock();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond, drawnThird, remaining));

        harness.enterBattlefieldAndReturn(player1, new HidetsuguAndKairi());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond, drawnThird);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, remaining);
    }

    @Test
    @DisplayName("An opponent gaining shroud prevents the entire death trigger from resolving")
    void illegalOpponentTargetPreventsExileAndLifeLoss() {
        Permanent source = addHidetsuguAndKairiWithResolvedEtb();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        kill(source);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
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
