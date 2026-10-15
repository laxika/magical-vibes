package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GuardianGladewalker.class, Forest.class})
class GuardianGladewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GuardianGladewalker());

        harness.setHand(player1, List.of(new GuardianGladewalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can target an opponent's creature")
    void etbCanTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianGladewalker());

        harness.setHand(player1, List.of(new GuardianGladewalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new GuardianGladewalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enter an empty battlefield and put its counter on itself")
    void canTargetItself() {
        harness.setHand(player1, List.of(new GuardianGladewalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent guardian = findPermanent(player1, "Guardian Gladewalker");
        harness.handlePermanentChosen(player1, guardian.getId());
        resolveAllTriggers();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("No counter is placed if the target leaves before the trigger resolves")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianGladewalker());
        harness.setHand(player1, List.of(new GuardianGladewalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Guardian Gladewalker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter trigger resolves even after its source leaves")
    void sourceLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianGladewalker());
        harness.setHand(player1, List.of(new GuardianGladewalker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Guardian Gladewalker"));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
