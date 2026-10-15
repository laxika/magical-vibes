package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongTheBrutishThespian.class, Shock.class, GrizzlyBears.class, Forest.class})
class StrongTheBrutishThespianTest extends BaseCardTest {

    @Test
    @DisplayName("Being dealt damage gives three rad counters and three +1/+1 counters")
    void damageTriggersEnrage() {
        Permanent strong = harness.addToBattlefieldAndReturn(player2, new StrongTheBrutishThespian());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, strong.getId());
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(strong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Radiation causes Strong's controller to gain life instead of lose life")
    void radiationCausesLifeGain() {
        harness.addToBattlefield(player1, new StrongTheBrutishThespian());
        harness.setLife(player1, 20);
        gd.playerRadCounters.put(player1.getId(), 2);
        List<com.github.laxika.magicalvibes.model.Card> library =
                List.of(new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, library);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerRadCounters.get(player1.getId())).isOne();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay two mana")
    void unpaidWardPreventsDamageAndEnrage() {
        Permanent strong = harness.addToBattlefieldAndReturn(player2, new StrongTheBrutishThespian());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, strong.getId());
        resolveAllTriggers();

        assertThat(strong.getMarkedDamage()).isZero();
        assertThat(strong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying exactly two mana for ward allows damage and enrage")
    void payingWardAllowsDamage() {
        Permanent strong = harness.addToBattlefieldAndReturn(player2, new StrongTheBrutishThespian());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, strong.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(strong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each separate damage event gives three counters regardless of damage amount")
    void separateDamageEventsTriggerSeparately() {
        Permanent strong = harness.addToBattlefieldAndReturn(player1, new StrongTheBrutishThespian());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, strong.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, strong.getId());
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(6);
        assertThat(strong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Simultaneous combat damage from two blockers triggers enrage only once")
    void simultaneousDamageTriggersOnce() {
        Permanent strong = addCreatureReady(player1, new StrongTheBrutishThespian());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 5));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        assertThat(strong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Strong, the Brutish Thespian");
    }

    @Test
    @DisplayName("Lethal combat damage still gives rad counters after Strong dies")
    void lethalDamageStillGivesRadCounters() {
        addCreatureReady(player1, new StrongTheBrutishThespian());
        addCreatureReady(player2, new StrongTheBrutishThespian());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Strong, the Brutish Thespian");
        harness.assertNotOnBattlefield(player2, "Strong, the Brutish Thespian");
        harness.assertInGraveyard(player1, "Strong, the Brutish Thespian");
        harness.assertInGraveyard(player2, "Strong, the Brutish Thespian");
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Strong does not replace an opponent's radiation life loss")
    void opponentsRadiationStillLosesLife() {
        harness.addToBattlefield(player1, new StrongTheBrutishThespian());
        harness.setLife(player2, 20);
        gd.playerRadCounters.put(player2.getId(), 2);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerRadCounters.get(player2.getId())).isOne();
    }

    @Test
    @DisplayName("Milling only lands neither gains life nor removes rad counters")
    void radiationMillingOnlyLandsDoesNotGainLife() {
        harness.addToBattlefield(player1, new StrongTheBrutishThespian());
        harness.setLife(player1, 20);
        gd.playerRadCounters.put(player1.getId(), 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Strong does not replace ordinary spell damage with life gain")
    void nonRadiationDamageStillLosesLife() {
        harness.addToBattlefield(player1, new StrongTheBrutishThespian());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isZero();
    }
}
