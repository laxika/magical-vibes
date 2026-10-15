package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrontlineDevastator.class})
class FrontlineDevastatorTest extends BaseCardTest {

    @Test
    void blockedCreatureMakesOnlyDefenderLoseTwoLife() {
        Permanent attacker = addCreatureReady(player1, new FrontlineDevastator());
        attacker.setAttacking(true);
        addCreatureReady(player2, new FrontlineDevastator());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void multipleBlockersCauseOnlyOneAfflictTrigger() {
        Permanent attacker = addCreatureReady(player1, new FrontlineDevastator());
        attacker.setAttacking(true);
        addCreatureReady(player2, new FrontlineDevastator());
        addCreatureReady(player2, new FrontlineDevastator());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void unblockedCreatureDealsCombatDamageWithoutAfflict() {
        Permanent attacker = addCreatureReady(player1, new FrontlineDevastator());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void abilityBoostsOnlyItsSourceAndDoesNotTapIt() {
        Permanent source = addCreatureReady(player1, new FrontlineDevastator());
        Permanent other = addCreatureReady(player1, new FrontlineDevastator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(source.getEffectiveToughness()).isEqualTo(3);
        assertThat(source.isTapped()).isFalse();
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void tappedSummoningSickCreatureCanActivateRepeatedlyAndBoostExpires() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FrontlineDevastator());
        source.setSummoningSick(true);
        source.tap();
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(5);
        assertThat(source.getEffectiveToughness()).isEqualTo(3);
        assertThat(source.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(source.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void activationRequiresTwoMana() {
        addCreatureReady(player1, new FrontlineDevastator());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresRedMana() {
        addCreatureReady(player1, new FrontlineDevastator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
