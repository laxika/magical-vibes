package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.s.SawbladeScamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSeedcore.class, GrizzlyBears.class, LlanowarElves.class,
        CrawlingChorus.class, SawbladeScamp.class, TyvarsStand.class})
class TheSeedcoreTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent seedcore = harness.addToBattlefieldAndReturn(player1, new TheSeedcore());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(seedcore.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds mana restricted to Phyrexian creature spells")
    void addsPhyrexianCreatureMana() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.PHYREXIAN), ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Phyrexian creature mana can cast a Phyrexian creature")
    void phyrexianCreatureManaCastsPhyrexianCreature() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new SawbladeScamp()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Phyrexian creature mana cannot cast a non-Phyrexian or noncreature spell")
    void phyrexianCreatureManaCannotCastOtherSpells() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(new LlanowarElves()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new TyvarsStand()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Corrupted ability boosts a 1/1 creature when an opponent has three poison counters")
    void corruptedBoostsOneOneCreature() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Corrupted ability cannot be activated without a poisoned opponent")
    void corruptedRequiresPoisonedOpponent() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("An opponent must have at least 3 poison counters");
    }

    @Test
    @DisplayName("Corrupted ability cannot target a creature that is not 1/1")
    void corruptedCannotTargetNonOneOneCreature() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Corrupted boost wears off at end of turn")
    void corruptedBoostWearsOff() {
        harness.addToBattlefieldAndReturn(player1, new TheSeedcore());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, elves.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    @Test
    void corruptedCanTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new TheSeedcore());
        Permanent target = addCreatureReady(player2, new CrawlingChorus());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void corruptedRequiresThreeCountersOnOpponentRatherThanController() {
        harness.addToBattlefield(player1, new TheSeedcore());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("An opponent must have at least 3 poison counters");
    }

    @Test
    void corruptedDoesNotRecheckPoisonCountersOnResolution() {
        harness.addToBattlefield(player1, new TheSeedcore());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void corruptedRejectsCreatureWithOnlyOnePower() {
        harness.addToBattlefield(player1, new TheSeedcore());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        target.setToughnessModifier(1);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    void corruptedRejectsCreatureWithOnlyOneToughness() {
        harness.addToBattlefield(player1, new TheSeedcore());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        target.setPowerModifier(1);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    void corruptedDoesNotResolveIfTargetStopsBeingOneOne() {
        harness.addToBattlefield(player1, new TheSeedcore());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
