package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlackcleaveGoblin;
import com.github.laxika.magicalvibes.cards.g.GrimAffliction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({MeliraSylvokOutcast.class, BlackcleaveGoblin.class, GrizzlyBears.class, IchorRats.class,
        GrimAffliction.class, TurnToFrog.class})
class MeliraSylvokOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent infect creature deals normal combat damage while Melira is present")
    void controllerTakesNormalDamageFromOpponentInfectCreature() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent goblin = addCreatureReady(player2, new BlackcleaveGoblin());
        goblin.setAttacking(true);
        harness.setLife(player1, 20);

        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Controller does not get poison counters from Ichor Rats ETB")
    void controllerDoesNotGetPoisonFromEffect() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());

        harness.setHand(player2, List.of(new IchorRats()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int poisonBefore = gd.playerPoisonCounters.getOrDefault(player1.getId(), 0);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // Player1 (with Melira) should not get poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0))
                .isEqualTo(poisonBefore);
    }

    @Test
    @DisplayName("Own creature cannot have -1/-1 counters placed on it")
    void ownCreatureCantGetMinusCounters() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Verify the creature has the granted effect
        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures are not protected from -1/-1 counters")
    void opponentCreatureCanGetMinusCounters() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("-1/-1 counter protection is lost when Melira leaves the battlefield")
    void minusCounterProtectionLostWhenMeliraRemoved() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isTrue();

        // Remove Melira
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Melira, Sylvok Outcast"));

        assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Opponent creature with infect loses infect")
    void opponentInfectCreatureLosesInfect() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new BlackcleaveGoblin());

        // Blackcleave Goblin normally has infect, but Melira removes it
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Own creature with infect keeps infect")
    void ownInfectCreatureKeepsInfect() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BlackcleaveGoblin());

        // Controller's own infect creatures should keep infect
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Infect removal is lost when Melira leaves the battlefield")
    void infectRemovalLostWhenMeliraRemoved() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new BlackcleaveGoblin());

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isFalse();

        // Remove Melira
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Melira, Sylvok Outcast"));

        // Goblin regains infect
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Melira prevents actual counter placement on herself")
    void preventsMinusCounterPlacementOnHerself() {
        Permanent melira = harness.addToBattlefieldAndReturn(player1, new MeliraSylvokOutcast());
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, melira.getId());

        assertThat(melira.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Melira, Sylvok Outcast");
    }

    @Test
    @DisplayName("Melira preserves existing counters but prevents proliferating them")
    void preservesExistingCountersAndPreventsProliferation() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), player1.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent creatures can actually receive minus counters")
    void allowsMinusCounterPlacementOnOpponentCreature() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Melira's abilities allows her controller to receive poison")
    void abilityLossEndsPoisonProtection() {
        Permanent melira = harness.addToBattlefieldAndReturn(player1, new MeliraSylvokOutcast());
        harness.setHand(player1, List.of(new TurnToFrog(), new IchorRats()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, melira.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("An allied creature remains protected when it loses its own abilities")
    void abilityLossOnProtectedCreatureDoesNotRemoveCounterRestriction() {
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog(), new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
