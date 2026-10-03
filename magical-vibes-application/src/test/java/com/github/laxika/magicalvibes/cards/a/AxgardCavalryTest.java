package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AxgardCavalry.class, Mountain.class})
class AxgardCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to give a target creature haste")
    void grantsHasteToTargetCreature() {
        Permanent cavalry = addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(cavalry.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new AxgardCavalry());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent cavalry = addCreatureReady(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, cavalry.getId());
        harness.passBothPriorities();

        assertThat(cavalry.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cavalry.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cavalry.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent cavalry = addCreatureReady(player1, new AxgardCavalry());
        harness.activateAbility(player1, 0, null, cavalry.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cavalry.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedHasteAllowsNewCreatureToActivateTapAbility() {
        Permanent cavalry = addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, cavalry.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.HASTE)).isTrue();
    }

    @Test
    void grantedHasteAllowsNewCreatureToAttack() {
        addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(target.isAttacking()).isTrue();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent cavalry = addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(cavalry);
        gd.playerGraveyards.get(player1.getId()).add(cavalry.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNotAffectTargetThatLeftBattlefield() {
        Permanent cavalry = addCreatureReady(player1, new AxgardCavalry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(cavalry.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.HASTE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
