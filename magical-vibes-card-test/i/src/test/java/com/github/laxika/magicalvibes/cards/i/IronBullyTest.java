package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArborealGrazer;
import com.github.laxika.magicalvibes.cards.m.ManaGeode;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronBully.class, ArborealGrazer.class, ManaGeode.class})
class IronBullyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can put a +1/+1 counter on an opponent's creature")
    void etbPutsCounterOnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaGeode());
        harness.setHand(player1, List.of(new IronBully()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast without a target when no creatures are on the battlefield")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new IronBully()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Bully");
        Permanent bully = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, bully.getId());
        assertThat(bully.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(bully.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter ability resolves even if Iron Bully leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        harness.setHand(player1, List.of(new IronBully()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof IronBully);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter ability does not retarget when its target leaves")
    void removedTargetReceivesNoCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());
        harness.setHand(player1, List.of(new IronBully()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace prevents blocking with one creature")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new IronBully());
        addCreatureReady(player2, new ArborealGrazer());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits blocking with two creatures")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new IronBully());
        Permanent first = addCreatureReady(player2, new ArborealGrazer());
        Permanent second = addCreatureReady(player2, new ArborealGrazer());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new IronBully()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
