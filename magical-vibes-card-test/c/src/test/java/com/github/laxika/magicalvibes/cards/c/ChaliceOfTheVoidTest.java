package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaliceOfTheVoid.class, MyrRetriever.class, Ornithopter.class, Shatter.class})
class ChaliceOfTheVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X charge counters")
    void entersWithXChargeCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent chalice = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters a spell with matching mana value cast by any player")
    void countersMatchingSpell() {
        Permanent chalice = addChalice(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        MyrRetriever spell = new MyrRetriever();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Myr Retriever");
        harness.assertInGraveyard(player2, "Myr Retriever");
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a spell with a nonmatching mana value")
    void doesNotCounterNonmatchingSpell() {
        addChalice(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        MyrRetriever spell = new MyrRetriever();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Myr Retriever");
        harness.assertNotInGraveyard(player2, "Myr Retriever");
    }

    @Test
    @DisplayName("Keeps a trigger after the charge counter count changes")
    void triggerUsesCounterCountWhenSpellWasCast() {
        Permanent chalice = addChalice(player1, 2);

        MyrRetriever spell = new MyrRetriever();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        chalice.setCounterCount(CounterType.CHARGE, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Myr Retriever");
        harness.assertInGraveyard(player2, "Myr Retriever");
    }

    @Test
    @DisplayName("Counters a matching spell cast by its controller")
    void countersMatchingSpellCastByController() {
        Permanent chalice = addChalice(player1, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        MyrRetriever spell = new MyrRetriever();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Myr Retriever");
        harness.assertInGraveyard(player1, "Myr Retriever");
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters a zero-mana spell when it has no charge counters")
    void countersZeroManaSpellWithNoChargeCounters() {
        addChalice(player1, 0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Uses both X symbols when checking a Chalice spell's mana value")
    void usesBothXSymbolsForManaValue() {
        addChalice(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ChaliceOfTheVoid()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chalice of the Void");
        harness.assertInGraveyard(player2, "Chalice of the Void");
    }

    @Test
    @DisplayName("A Chalice cast for zero enters without countering itself")
    void zeroChaliceDoesNotCounterItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chalice of the Void");
        harness.assertNotInGraveyard(player1, "Chalice of the Void");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Destroying Chalice does not stop its pending trigger")
    void countersSpellAfterSourceIsDestroyed() {
        Permanent chalice = addChalice(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.castInstant(player1, 0, chalice.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Chalice of the Void");
        harness.assertInGraveyard(player1, "Chalice of the Void");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Adding counters after a nonmatching spell was cast does not create a trigger")
    void doesNotTriggerRetroactivelyWhenCountersChange() {
        Permanent chalice = addChalice(player1, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MyrRetriever()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        chalice.setCounterCount(CounterType.CHARGE, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Myr Retriever");
        harness.assertNotInGraveyard(player2, "Myr Retriever");
    }

    private Permanent addChalice(Player player, int chargeCounters) {
        Permanent chalice = harness.addToBattlefieldAndReturn(player, new ChaliceOfTheVoid());
        chalice.setCounterCount(CounterType.CHARGE, chargeCounters);
        return chalice;
    }

}
