package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorderlandRanger;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({TimberlandGuide.class, BorderlandRanger.class, Mountain.class})
class TimberlandGuideTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BorderlandRanger());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB can target an opponent's creature")
    void etbCanTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BorderlandRanger());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enter an empty battlefield and target itself")
    void canTargetItself() {
        harness.castFromHand(player1, new TimberlandGuide(), "{1}{G}");
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Timberland Guide");
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, guide.getId());
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Target leaving before resolution receives no counter")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorderlandRanger());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Timberland Guide")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger resolves independently after its source leaves")
    void sourceLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorderlandRanger());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Timberland Guide");
        gd.playerBattlefields.get(player1.getId()).remove(guide);
        harness.setGraveyard(player1, List.of(guide.getCard()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast still triggers counter placement")
    void entersWithoutBeingCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorderlandRanger());

        Permanent guide = harness.enterBattlefieldAndReturn(player1, new TimberlandGuide());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
