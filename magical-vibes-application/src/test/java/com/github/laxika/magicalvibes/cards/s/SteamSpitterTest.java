package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteamSpitter.class, BorealGriffin.class})
class SteamSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent spider = addCreatureReady(player1, new SteamSpitter());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent spider = addCreatureReady(player1, new SteamSpitter());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(2);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new SteamSpitter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent spider = addCreatureReady(player1, new SteamSpitter());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(0);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not require tapping")
    void canActivateWhileSummoningSick() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SteamSpitter());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Reach allows Steam Spitter to block a creature with flying")
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new BorealGriffin());
        Permanent blocker = addCreatureReady(player2, new SteamSpitter());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("A tapped Steam Spitter can activate and boosts only itself")
    void tappedSourceBoostsOnlyItself() {
        Permanent otherSpider = addCreatureReady(player1, new SteamSpitter());
        Permanent source = addCreatureReady(player1, new SteamSpitter());
        source.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(source.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(source.isTapped()).isTrue();
        assertThat(otherSpider.getPowerModifier()).isZero();
        assertThat(otherSpider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple activations can be stacked before either resolves")
    void activationsStackBeforeResolution() {
        Permanent spider = addCreatureReady(player1, new SteamSpitter());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(spider.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(spider.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(spider.getPowerModifier()).isEqualTo(2);
        assertThat(spider.getToughnessModifier()).isZero();
    }

}
