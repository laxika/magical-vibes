package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinersBane.class})
class MinersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +1/+0 and trample")
    void abilityGrantsBoostAndTrample() {
        Permanent bane = addBane(player1);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bane)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Activating twice stacks the boosts")
    void activatingTwiceStacksBoosts() {
        Permanent bane = addBane(player1);
        addCost(player1);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bane = addBane(player1);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addBane(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent bane = harness.addToBattlefieldAndReturn(player1, new MinersBane());
        bane.setSummoningSick(true);
        bane.tap();
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isTrue();
        assertThat(bane.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability does not apply until it resolves and affects only its source")
    void affectsOnlySourceOnResolution() {
        Permanent bane = addBane(player1);
        Permanent other = addBane(player1);
        Permanent opposing = addBane(player2);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Three generic mana cannot pay the red requirement")
    void requiresRedMana() {
        addBane(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Granted trample deals excess combat damage even when the source dies")
    void grantedTrampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addBane(player1);
        Permanent blocker = addBane(player2);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addBane(Player player) {
        return addCreatureReady(player, new MinersBane());
    }

    private void addCost(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
