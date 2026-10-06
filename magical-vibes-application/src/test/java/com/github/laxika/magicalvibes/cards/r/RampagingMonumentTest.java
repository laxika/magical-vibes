package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorosChallenger;
import com.github.laxika.magicalvibes.cards.d.DevkarinDissident;
import com.github.laxika.magicalvibes.cards.f.FreshFacedRecruit;
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

@CardUsed({RampagingMonument.class, BorosChallenger.class, DevkarinDissident.class, FreshFacedRecruit.class})
class RampagingMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new RampagingMonument()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent monument = findMonument();
        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(monument.getEffectivePower()).isEqualTo(3);
        assertThat(monument.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when its controller casts a multicolored spell")
    void multicoloredSpellAddsCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.setHand(player1, List.of(new BorosChallenger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for a monocolored spell")
    void monocoloredSpellDoesNotAddCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.setHand(player1, List.of(new DevkarinDissident()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's multicolored spell")
    void opponentMulticoloredSpellDoesNotAddCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BorosChallenger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Hybrid spell paid with only red mana triggers before the spell resolves")
    void hybridSpellTriggersBeforeResolving() {
        Permanent monument = addMonumentWithCounters();
        harness.setHand(player1, List.of(new FreshFacedRecruit()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Fresh-Faced Recruit");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fresh-Faced Recruit");
        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting another Monument does not trigger the existing Monument")
    void colorlessSpellDoesNotAddCounter() {
        Permanent monument = addMonumentWithCounters();
        harness.setHand(player1, List.of(new RampagingMonument()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(monument.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each Monument gets its own counter for a multicolored spell")
    void eachMonumentTriggersIndependently() {
        Permanent first = addMonumentWithCounters();
        Permanent second = addMonumentWithCounters();
        harness.setHand(player1, List.of(new BorosChallenger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Boros Challenger");
    }

    @Test
    @DisplayName("Trample deals excess damage beyond a blocker using its entry counters")
    void trampleDealsExcessDamage() {
        harness.setLife(player2, 20);
        Permanent monument = addMonumentWithCounters();
        monument.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new DevkarinDissident());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Devkarin Dissident");
        harness.assertOnBattlefield(player1, "Rampaging Monument");
    }

    private Permanent addMonumentWithCounters() {
        return harness.enterBattlefieldAndReturn(player1, new RampagingMonument());
    }

    private Permanent findMonument() {
        return findPermanent(player1, "Rampaging Monument");
    }
}
