package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlchemaxSlayerBots.class})
class AlchemaxSlayerBotsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps and stuns target creature an opponent controls")
    void etbTapsAndStunsTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemaxSlayerBots());
        harness.setHand(player1, List.of(new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a creature you control")
    void cannotTargetYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlchemaxSlayerBots());
        harness.setHand(player1, List.of(new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB adds a stun counter even when the target is already tapped")
    void etbStunsAlreadyTappedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemaxSlayerBots());
        target.tap();
        harness.setHand(player1, List.of(new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }
    @Test
    @DisplayName("Creature can enter when no opponent controls a creature")
    void entersWithoutAnOpponentCreature() {
        harness.castFromHand(player1, new AlchemaxSlayerBots(), "{2}{U}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Alchemax Slayer-Bots");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Stun counter replaces the next untap, then the creature untaps normally")
    void stunReplacesOneUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemaxSlayerBots());
        harness.setHand(player1, List.of(new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated enters abilities add stun counters that each replace an untap")
    void multipleStunCountersReplaceSeparateUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemaxSlayerBots());
        harness.setHand(player1, List.of(new AlchemaxSlayerBots(), new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters ability does nothing if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemaxSlayerBots());
        harness.setHand(player1, List.of(new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.assertOnBattlefield(player1, "Alchemax Slayer-Bots");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters ability resolves even when its source leaves the battlefield")
    void sourceLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemaxSlayerBots());
        harness.setHand(player1, List.of(new AlchemaxSlayerBots()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Alchemax Slayer-Bots"));
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }
}
