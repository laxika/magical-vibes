package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WatcherOfTheSpheres.class, SerraAngel.class, GrizzlyBears.class, Levitation.class})
class WatcherOfTheSpheresTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creature spells you cast cost {1} less")
    void flyingCreatureSpellIsReduced() {
        harness.addToBattlefield(player1, new WatcherOfTheSpheres());
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serra Angel");
    }

    @Test
    @DisplayName("Creature spells without flying are not reduced")
    void nonFlyingCreatureSpellIsNotReduced() {
        harness.addToBattlefield(player1, new WatcherOfTheSpheres());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another flying creature enters")
    void flyingCreatureEnteringBoostsWatcher() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheSpheres());
        harness.castFromHand(player1, new SerraAngel(), "{W}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonflying creature entering does not boost Watcher of the Spheres")
    void nonFlyingCreatureEnteringDoesNotBoostWatcher() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheSpheres());
        harness.castFromHand(player1, new GrizzlyBears(), "{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("The flying creature boost wears off at end of turn")
    void flyingCreatureBoostWearsOffAtEndOfTurn() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheSpheres());
        harness.castFromHand(player1, new SerraAngel(), "{W}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    void enteringWatcherDoesNotTriggerItself() {
        harness.castFromHand(player1, new WatcherOfTheSpheres(), "{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent watcher = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    void opponentFlyingCreatureDoesNotTriggerWatcher() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheSpheres());
        harness.enterBattlefieldAndReturn(player2, new SerraAngel());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    void multipleWatchersStackTheirCostReductions() {
        harness.addToBattlefield(player1, new WatcherOfTheSpheres());
        harness.addToBattlefield(player1, new WatcherOfTheSpheres());

        harness.castFromHand(player1, new SerraAngel(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void costReductionDoesNotRemoveColoredMana() {
        harness.addToBattlefield(player1, new WatcherOfTheSpheres());
        harness.setHand(player1, List.of(new WatcherOfTheSpheres()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentFlyingSpellIsNotReduced() {
        harness.addToBattlefield(player1, new WatcherOfTheSpheres());

        assertThatThrownBy(() -> harness.castFromHand(player2, new SerraAngel(), "{2}{W}{W}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleFlyingEntriesGiveSeparateBoosts() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheSpheres());
        harness.enterBattlefieldAndReturn(player1, new SerraAngel());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new SerraAngel());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(4);
    }

    @Test
    void creatureWithFlyingGrantedByLevitationTriggersWatcher() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new WatcherOfTheSpheres());
        harness.addToBattlefield(player1, new Levitation());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(3);
    }
}
