package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DazzlingBeauty;
import com.github.laxika.magicalvibes.cards.f.FlashFoliage;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Retaliation.class, GorillaWarrior.class})
class RetaliationTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control gets +1/+1 when it becomes blocked")
    void ownCreatureBecomingBlockedGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Retaliation());
        addCreatureReady(player2, new GorillaWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("An unblocked creature you control does not get the boost")
    void ownUnblockedCreatureDoesNotGetBoost() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Retaliation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A creature an opponent controls does not get Retaliation's trigger")
    void opponentCreatureDoesNotGetGrant() {
        addCreatureReady(player1, new Retaliation());
        Permanent attacker = addCreatureReady(player2, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GorillaWarrior());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A creature you control gets the boost once for each creature blocking it")
    void ownCreatureBecomingBlockedByMultipleCreaturesGetsBoostForEachBlocker() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Retaliation());
        addCreatureReady(player2, new GorillaWarrior());
        addCreatureReady(player2, new GorillaWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Retaliation());
        addCreatureReady(player2, new GorillaWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed(DazzlingBeauty.class)
    @DisplayName("Becoming blocked without a creature does not trigger Retaliation")
    void blockedWithoutCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Retaliation());
        addCreatureReady(player2, new GorillaWarrior());

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new DazzlingBeauty()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed(FlashFoliage.class)
    @DisplayName("A creature entering the battlefield blocking an attacker triggers Retaliation")
    void creatureEnteringAsBlockerTriggers() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Retaliation());
        addCreatureReady(player2, new GorillaWarrior());

        declareAttackers(List.of(0));
        harness.setHand(player2, List.of(new FlashFoliage()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }
}
