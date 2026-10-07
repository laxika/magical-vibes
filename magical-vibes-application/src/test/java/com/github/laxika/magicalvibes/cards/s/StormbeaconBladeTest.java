package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FortressKinGuard;
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

@CardUsed({StormbeaconBlade.class, FortressKinGuard.class})
class StormbeaconBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new FortressKinGuard());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new StormbeaconBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws when three creatures you control attack")
    void drawsWhenThreeControlledCreaturesAttack() {
        setUpBattlefieldAndLibrary();
        Permanent blade = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent equippedCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        blade.setAttachedTo(equippedCreature.getId());

        declareAttackers(player1, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when fewer than three creatures you control attack")
    void doesNotDrawWhenFewerThanThreeControlledCreaturesAttack() {
        setUpBattlefieldAndLibrary();
        Permanent blade = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent equippedCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        blade.setAttachedTo(equippedCreature.getId());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when the Blade is unattached")
    void doesNotTriggerWhenUnattached() {
        setUpBattlefieldAndLibrary();

        declareAttackers(player1, List.of(1, 2, 3));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Stormbeacon Blade"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void equipCostsTwoManaAndMovesThePowerBonus() {
        Permanent first = addCreatureReady(player1, new FortressKinGuard());
        Permanent second = addCreatureReady(player1, new FortressKinGuard());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new StormbeaconBlade());
        blade.setAttachedTo(first.getId());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void equipCannotBePaidWithOnlyOneMana() {
        Permanent creature = addCreatureReady(player1, new FortressKinGuard());
        harness.addToBattlefield(player1, new StormbeaconBlade());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new FortressKinGuard());
        harness.addToBattlefield(player1, new StormbeaconBlade());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new FortressKinGuard());
        harness.addToBattlefield(player1, new StormbeaconBlade());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noTriggerWhenEquippedCreatureDoesNotAttack() {
        setUpBattlefieldAndLibrary();
        Permanent blade = findPermanent(player1, "Stormbeacon Blade");
        blade.setAttachedTo(gd.playerBattlefields.get(player1.getId()).get(1).getId());
        addCreatureReady(player1, new FortressKinGuard());

        declareAttackers(List.of(2, 3, 4));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void triggersBelowThresholdAndCountsCreaturesEnteringAttackingBeforeResolution() {
        setUpBattlefieldAndLibrary();
        Permanent blade = findPermanent(player1, "Stormbeacon Blade");
        blade.setAttachedTo(gd.playerBattlefields.get(player1.getId()).get(1).getId());

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).hasSize(1);
        Permanent additionalAttacker = addCreatureReady(player1, new FortressKinGuard());
        additionalAttacker.setAttacking(true);
        additionalAttacker.setAttackTarget(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawIfAnAttackerLeavesBeforeResolution() {
        setUpBattlefieldAndLibrary();
        Permanent blade = findPermanent(player1, "Stormbeacon Blade");
        blade.setAttachedTo(gd.playerBattlefields.get(player1.getId()).get(1).getId());

        declareAttackers(List.of(1, 2, 3));
        assertThat(gd.stack).hasSize(1);
        Permanent departingAttacker = gd.playerBattlefields.get(player1.getId()).remove(3);
        gd.playerGraveyards.get(player1.getId()).add(departingAttacker.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void removingEquipmentDoesNotStopAnAlreadyTriggeredDraw() {
        setUpBattlefieldAndLibrary();
        Permanent blade = findPermanent(player1, "Stormbeacon Blade");
        blade.setAttachedTo(gd.playerBattlefields.get(player1.getId()).get(1).getId());

        declareAttackers(List.of(1, 2, 3));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(blade);
        gd.playerGraveyards.get(player1.getId()).add(blade.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opposingEquippedCreatureTriggersButItsAttackersDoNotSatisfyTheCondition() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new StormbeaconBlade());
        Permanent opposingCreature = addCreatureReady(player2, new FortressKinGuard());
        addCreatureReady(player2, new FortressKinGuard());
        addCreatureReady(player2, new FortressKinGuard());
        blade.setAttachedTo(opposingCreature.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new StormbeaconBlade()));

        declareAttackers(player2, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void setUpBattlefieldAndLibrary() {
        harness.addToBattlefield(player1, new StormbeaconBlade());
        addCreatureReady(player1, new FortressKinGuard());
        addCreatureReady(player1, new FortressKinGuard());
        addCreatureReady(player1, new FortressKinGuard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StormbeaconBlade()));
    }
}
