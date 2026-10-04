package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.g.GoblinFurrier;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuryOfTheHorde.class, GoblinFurrier.class, BorealDruid.class, VedalkenOrrery.class})
class FuryOfTheHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps creatures that attacked this turn and grants an additional combat and main phase")
    void untapsAttackedCreaturesAndGrantsAdditionalCombatAndMainPhase() {
        Permanent attackedFurrier = addCreatureReady(player1, new GoblinFurrier());
        Permanent nonAttackedFurrier = addCreatureReady(player1, new GoblinFurrier());

        declareAttackers(List.of(0));
        nonAttackedFurrier.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(attackedFurrier.isTapped()).isFalse();
        assertThat(nonAttackedFurrier.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast by exiling two red cards from hand instead of paying mana")
    void castsByExilingTwoRedCardsFromHand() {
        harness.setHand(player1, List.of(new FuryOfTheHorde(), new GoblinFurrier(), new GoblinFurrier()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Goblin Furrier", "Goblin Furrier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires two red cards from hand")
    void alternateCostRequiresTwoRedCards() {
        harness.setHand(player1, List.of(new FuryOfTheHorde(), new GoblinFurrier(), new BorealDruid()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Additional combat begins after the resolving main phase")
    void additionalCombatBeginsAfterResolvingMainPhase() {
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Alternate cost cannot exile the spell itself")
    void cannotExileItselfToPayAlternateCost() {
        harness.setHand(player1, List.of(new FuryOfTheHorde(), new GoblinFurrier()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires two distinct red cards")
    void cannotExileSameCardTwice() {
        harness.setHand(player1, List.of(new FuryOfTheHorde(), new GoblinFurrier(), new GoblinFurrier()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost supports cards on either side of the spell in hand")
    void exilesCardsBeforeAndAfterSpellInHand() {
        GoblinFurrier first = new GoblinFurrier();
        GoblinFurrier second = new GoblinFurrier();
        BorealDruid remaining = new BorealDruid();
        harness.setHand(player1, List.of(first, new FuryOfTheHorde(), second, remaining));

        harness.castInstantWithAlternateExileFromHand(player1, 1, null, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player1, "Fury of the Horde");
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @CardUsed(VedalkenOrrery.class)
    @DisplayName("Resolving outside a main phase untaps attackers but creates no phases")
    void resolvingDuringCombatDoesNotCreateAdditionalPhases() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent attacker = addCreatureReady(player1, new GoblinFurrier());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Casting before combat preserves the regular combat after the added main phase")
    void precombatCastingPreservesRegularCombat() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @CardUsed(VedalkenOrrery.class)
    @DisplayName("Untaps an opponent's creatures that attacked this turn")
    void untapsOpponentsAttackers() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent attacker = addCreatureReady(player2, new GoblinFurrier());
        Permanent nonAttacker = addCreatureReady(player2, new GoblinFurrier());
        declareAttackers(player2, List.of(0));
        nonAttacker.tap();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FuryOfTheHorde()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(nonAttacker.isTapped()).isTrue();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

}
