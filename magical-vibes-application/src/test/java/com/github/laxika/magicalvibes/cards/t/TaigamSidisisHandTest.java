package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaigamSidisisHand.class, GrizzlyBears.class, Shock.class, Forest.class})
class TaigamSidisisHandTest extends BaseCardTest {

    @Test
    void upkeepPutsOneOfTheTopThreeIntoHandAndTheRestIntoGraveyard() {
        Card first = new Shock();
        Card chosen = new Shock();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.addToBattlefield(player1, new TaigamSidisisHand());

        advanceTaigamToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void controllerSkipsTheirDrawStep() {
        harness.addToBattlefield(player1, new TaigamSidisisHand());
        gd.playerDecks.get(player1.getId()).clear();
        gd.turnNumber = 2;
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceTaigamToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    void exiledGraveyardCardsScaleTheTemporaryDebuff() {
        Permanent taigam = addCreatureReady(player1, new TaigamSidisisHand());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(taigam.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isZero();
    }

    @Test
    void activatedAbilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TaigamSidisisHand());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceTaigamToUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
