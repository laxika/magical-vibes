package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({SireOfSevenDeaths.class, Shock.class, MightOfOaks.class, SerraAngel.class,
        LlanowarElves.class, ProdigalPyromancer.class, EverybodyLives.class})
class SireOfSevenDeathsTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller declines to pay 7 life")
    void wardCountersSpellWhenPaymentIsDeclined() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sire.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Sire of Seven Deaths");
    }

    @Test
    @DisplayName("Paying 7 life allows the targeting spell to resolve")
    void payingWardLifeCostAllowsSpellToResolve() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MightOfOaks()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, sire.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        assertThat(gqs.getEffectivePower(gd, sire)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, sire)).isEqualTo(14);
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, sire.getId());

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, sire)).isEqualTo(14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void wardCountersSpellWhenOpponentCannotPaySevenLife() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());
        harness.setLife(player2, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sire.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 6);
        assertThat(sire.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void unblockedAttackGainsLifeOnceAndDoesNotTap() {
        Permanent sire = addCreatureReady(player1, new SireOfSevenDeaths());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(sire.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 13);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new SireOfSevenDeaths());
        addCreatureReady(player2, new LlanowarElves());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void firstStrikeKillsTwoBlockersAndTramplesWithLifelink() {
        Permanent sire = addCreatureReady(player1, new SireOfSevenDeaths());
        Permanent first = addCreatureReady(player2, new LlanowarElves());
        Permanent second = addCreatureReady(player2, new LlanowarElves());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(first.getId(), 1, second.getId(), 1, player2.getId(), 5));

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Sire of Seven Deaths");
        assertThat(sire.getMarkedDamage()).isZero();
        harness.assertLife(player1, 27);
        harness.assertLife(player2, 15);
    }

    @Test
    void reachAllowsBlockingFlyingAndFirstStrikePreventsReturnDamage() {
        addCreatureReady(player1, new SerraAngel());
        Permanent sire = addCreatureReady(player2, new SireOfSevenDeaths());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertOnBattlefield(player2, "Sire of Seven Deaths");
        assertThat(sire.getMarkedDamage()).isZero();
        harness.assertLife(player2, 27);
        harness.assertLife(player1, 20);
    }

    @Test
    void wardCountersOpposingActivatedAbilityWhenPaymentIsDeclined() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, sire.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(sire.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        harness.assertLife(player2, 20);
    }

    @Test
    void payingWardAllowsOpposingActivatedAbilityToResolve() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, sire.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        assertThat(sire.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void wardLifeCostCannotBePaidWhenLifeLossIsProhibited() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new EverybodyLives()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, sire.getId());
        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Sire of Seven Deaths");
    }
}
