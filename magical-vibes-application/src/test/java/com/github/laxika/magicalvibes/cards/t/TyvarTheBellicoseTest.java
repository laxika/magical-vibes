package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.cards.p.PriestOfTitania;
import com.github.laxika.magicalvibes.cards.v.ViridianJoiner;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TyvarTheBellicose.class, GrizzlyBears.class, LlanowarElves.class, PriestOfTitania.class,
        MomentaryBlink.class, TurnToFrog.class, ViridianJoiner.class, GiantGrowth.class, GoldMyr.class})
class TyvarTheBellicoseTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Elves gain deathtouch until end of turn")
    void attackingElvesGainDeathtouch() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Each creature gets counters from its first mana ability each turn")
    void manaAbilityResolutionPutsCountersOnEachSource() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent priest = addCreatureReady(player1, new PriestOfTitania());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.tapPermanent(player1, 2);
        resolveAllTriggers();

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature's second mana ability resolution in the same turn adds no counters")
    void secondManaAbilityDoesNotAddCounters() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        elf.untap();
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blinking Tyvar grants a fresh ability that may trigger again this turn")
    void blinkingTyvarAllowsAnotherManaTrigger() {
        Permanent tyvar = harness.addToBattlefieldAndReturn(player1, new TyvarTheBellicose());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, tyvar.getId());

        elf.untap();
        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with no Elves does not trigger Tyvar")
    void attackingWithOnlyNonElvesDoesNotTrigger() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All attacking Elves, including Tyvar, gain deathtouch; other creatures do not")
    void grantsDeathtouchOnlyToAttackingElves() {
        Permanent tyvar = addCreatureReady(player1, new TyvarTheBellicose());
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        Permanent nonattacker = addCreatureReady(player1, new LlanowarElves());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1, 3)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, tyvar, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An Elf that becomes a Frog in response still receives deathtouch")
    void attackingElfThatChangesSubtypeStillGainsDeathtouch() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, elf.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A zero-mana resolution consumes the granted ability's trigger for the turn")
    void zeroManaResolutionConsumesOncePerTurnTrigger() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());
        joiner.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();
        assertThat(joiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, joiner.getId());
        joiner.untap();
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(joiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(joiner.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Elf creatures receive the mana trigger but opposing creatures do not")
    void manaTriggerAppliesOnlyToControlledCreatures() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent myr = addCreatureReady(player1, new GoldMyr());
        Permanent opposingElf = addCreatureReady(player2, new LlanowarElves());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The mana trigger becomes available again on the opponent's turn")
    void manaTriggerResetsEachTurn() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        elf.untap();
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
