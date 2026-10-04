package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GargoyleSentinel.class})
class GargoyleSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(sentinel.getId());
    }

    @Test
    @DisplayName("Resolving ability removes defender and grants flying until end of turn")
    void resolvingAbilityRemovesDefenderAndGrantsFlying() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Defender and flying reset at end of turn")
    void defenderAndFlyingResetAtEndOfTurn() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot attack without activating ability (has defender)")
    void cannotAttackWithDefender() {
        addSentinelReady(player1);
        harness.addToBattlefield(player2, new GargoyleSentinel());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack after activating ability (defender removed)")
    void canAttackAfterActivatingAbility() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player2, new GargoyleSentinel());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(sentinel.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Gargoyle Sentinel")
    void activatingAbilityDoesNotTap() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addSentinelReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability resolves without affecting a replacement after its source leaves")
    void abilityDoesNotAffectReplacementAfterSourceRemoved() {
        addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = addSentinelReady(player1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, replacement, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FLYING)).isFalse();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GargoyleSentinel());
        sentinel.setSummoningSick(true);
        sentinel.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability affects only its source, including after repeated activation")
    void repeatedActivationAffectsOnlySource() {
        Permanent sentinel = addSentinelReady(player1);
        Permanent other = addSentinelReady(player1);
        Permanent opposing = addSentinelReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isTrue();
        for (Permanent unaffected : List.of(other, opposing)) {
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.DEFENDER)).isTrue();
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.FLYING)).isFalse();
        }

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying prevents a ground creature from blocking")
    void grantedFlyingPreventsGroundBlocker() {
        addSentinelReady(player1);
        Permanent blocker = addSentinelReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    private Permanent addSentinelReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GargoyleSentinel());
        perm.setSummoningSick(false);
        return perm;
    }
}
