package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
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

@CardUsed({
        HeliodTheRadiantDawn.class,
        HeliodTheWarpedEclipse.class,
        Cessation.class,
        ErebosGodOfTheDead.class,
        Divination.class
})
class HeliodTheRadiantDawnTest extends BaseCardTest {

    @Test
    void returnsTargetNonGodEnchantmentFromGraveyardToHand() {
        Card cessation = new Cessation();
        Card god = new ErebosGodOfTheDead();
        harness.setGraveyard(player1, List.of(cessation, god));
        harness.setHand(player1, List.of(new HeliodTheRadiantDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(cessation.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cessation);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(god);
    }

    @Test
    void transformAbilityUsesSorceryTiming() {
        Permanent heliod = addHeliod();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(heliod.isTransformed()).isTrue();
    }

    @Test
    void warpedEclipseGrantsFlashAndReducesCostsByCardsOpponentsDrew() {
        addTransformedHeliod();
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void warpedEclipseLetsControllerCastSorceriesDuringOpponentTurn() {
        addTransformedHeliod();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void transformCanBePaidWithLifeWhileSummoningSick() {
        Permanent heliod = harness.addToBattlefieldAndReturn(player1, new HeliodTheRadiantDawn());
        heliod.setSummoningSick(true);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(heliod.isTransformed()).isFalse();
        harness.passBothPriorities();
        assertThat(heliod.isTransformed()).isTrue();
    }

    @Test
    void cannotTransformDuringCombat() {
        Permanent heliod = addHeliod();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heliod.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashPermissionDoesNotAllowTransformingWithANonemptyStack() {
        addTransformedHeliod();
        Permanent front = harness.addToBattlefieldAndReturn(player1, new HeliodTheRadiantDawn());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(front.isTransformed()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void enteringWithoutLegalTargetsDoesNotReturnGodsOrSorceries() {
        Card god = new HeliodTheRadiantDawn();
        Card sorcery = new Divination();
        harness.setGraveyard(player1, List.of(god, sorcery));
        harness.setHand(player1, List.of(new HeliodTheRadiantDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(god, sorcery);
    }

    @Test
    void cannotReturnAnEnchantmentFromAnOpponentsGraveyard() {
        Card enchantment = new Cessation();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new HeliodTheRadiantDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(enchantment);
    }

    @Test
    void returnAbilityDoesNotChooseANewTargetIfOriginalTargetLeavesGraveyard() {
        Card target = new Cessation();
        Card other = new Cessation();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new HeliodTheRadiantDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllersDrawsDoNotReduceSpellCosts() {
        addTransformedHeliod();
        gd.cardsDrawnThisTurn.put(player1.getId(), 5);
        gd.cardsDrawnThisTurn.put(player2.getId(), 0);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reductionDoesNotPayColoredManaEvenWhenOpponentsDrewManyCards() {
        addTransformedHeliod();
        gd.cardsDrawnThisTurn.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsBeforeTransformationCountForCostReduction() {
        addHeliod();
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentDoesNotReceiveTheCostReduction() {
        addTransformedHeliod();
        gd.cardsDrawnThisTurn.put(player1.getId(), 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentDoesNotReceiveFlashPermission() {
        addTransformedHeliod();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTransformDuringOpponentsMainPhase() {
        Permanent heliod = addHeliod();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heliod.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addHeliod() {
        Permanent heliod = harness.addToBattlefieldAndReturn(player1, new HeliodTheRadiantDawn());
        heliod.setSummoningSick(false);
        return heliod;
    }

    private Permanent addTransformedHeliod() {
        Permanent heliod = addHeliod();
        heliod.setCard(heliod.getCard().getBackFaceCard());
        heliod.setTransformed(true);
        return heliod;
    }
}
