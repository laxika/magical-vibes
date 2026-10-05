package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoreSwine;
import com.github.laxika.magicalvibes.cards.s.ShockmawDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KolaghanTheStormsFury.class, GoreSwine.class, ShockmawDragon.class})
class KolaghanTheStormsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("A Dragon attacking boosts all creatures you control")
    void attackingDragonBoostsOwnCreatures() {
        Permanent kolaghan = addCreatureReady(player1, new KolaghanTheStormsFury());
        Permanent bears = addCreatureReady(player1, new GoreSwine());
        Permanent opponent = addCreatureReady(player2, new GoreSwine());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(kolaghan.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(kolaghan.getToughnessModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A non-Dragon attacking does not trigger the boost")
    void attackingNonDragonDoesNotBoost() {
        addCreatureReady(player1, new KolaghanTheStormsFury());
        Permanent bears = addCreatureReady(player1, new GoreSwine());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KolaghanTheStormsFury());
        Permanent bears = addCreatureReady(player1, new GoreSwine());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Dash grants haste and returns Kolaghan to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new KolaghanTheStormsFury()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent kolaghan = findPermanent(player1, "Kolaghan, the Storm's Fury");
        assertThat(kolaghan.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player1, "Kolaghan, the Storm's Fury");
        harness.assertNotOnBattlefield(player1, "Kolaghan, the Storm's Fury");
    }

    @Test
    void eachAttackingDragonAddsOneBoostIncludingWhenKolaghanDoesNotAttack() {
        Permanent kolaghan = addCreatureReady(player1, new KolaghanTheStormsFury());
        Permanent firstDragon = addCreatureReady(player1, new ShockmawDragon());
        Permanent secondDragon = addCreatureReady(player1, new ShockmawDragon());
        Permanent swine = addCreatureReady(player1, new GoreSwine());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(kolaghan.getPowerModifier()).isEqualTo(2);
        assertThat(firstDragon.getPowerModifier()).isEqualTo(2);
        assertThat(secondDragon.getPowerModifier()).isEqualTo(2);
        assertThat(swine.getPowerModifier()).isEqualTo(2);
        assertThat(swine.getToughnessModifier()).isZero();
    }

    @Test
    void opposingDragonDoesNotTriggerBoost() {
        Permanent kolaghan = addCreatureReady(player1, new KolaghanTheStormsFury());
        Permanent swine = addCreatureReady(player1, new GoreSwine());
        addCreatureReady(player2, new ShockmawDragon());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(kolaghan.getPowerModifier()).isZero();
        assertThat(swine.getPowerModifier()).isZero();
    }

    @Test
    void normalCastDoesNotGrantHasteOrReturnAtEndStep() {
        harness.castFromHand(player1, new KolaghanTheStormsFury(), "{3}{B}{R}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Kolaghan, the Storm's Fury").hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Kolaghan, the Storm's Fury");
        harness.assertNotInHand(player1, "Kolaghan, the Storm's Fury");
    }

    @Test
    void dashDoesNotCreateAnEntersTheBattlefieldTrigger() {
        harness.setHand(player1, List.of(new KolaghanTheStormsFury()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kolaghan, the Storm's Fury");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dashReturnWaitsForEndStepTriggerToResolve() {
        harness.setHand(player1, List.of(new KolaghanTheStormsFury()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Kolaghan, the Storm's Fury");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kolaghan, the Storm's Fury");
        harness.assertInHand(player1, "Kolaghan, the Storm's Fury");
    }
}
