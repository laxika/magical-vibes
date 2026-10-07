package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.l.Lull;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitaniasChosen.class, GorillaWarrior.class, GoblinRaider.class, Lull.class, Putrefy.class})
class TitaniasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when its controller casts a green spell")
    void controllerCastingGreenSpellAddsCounter() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent casts a green spell")
    void opponentCastingGreenSpellAddsCounter() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GorillaWarrior(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when a player casts a non-green spell")
    void nonGreenSpellDoesNotAddCounter() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GoblinRaider(), "{1}{R}");

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a player casts a green noncreature spell")
    void noncreatureGreenSpellAddsCounter() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Lull(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a player casts a multicolored green spell")
    void multicoloredGreenSpellAddsCounter() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());
        Permanent target = addCreatureReady(player2, new GorillaWarrior());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter is added by a trigger before the green spell resolves")
    void counterTriggerResolvesBeforeSpell() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        harness.passBothPriorities();

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Chosen triggers an existing Chosen but not the one being cast")
    void doesNotTriggerForItsOwnCast() {
        Permanent existing = addCreatureReady(player1, new TitaniasChosen());
        TitaniasChosen incoming = new TitaniasChosen();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, incoming, "{2}{G}");
        resolveAllTriggers();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(incoming.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each player's Chosen gets one counter for each green spell")
    void multipleChosenTriggerForRepeatedGreenSpells() {
        Permanent first = addCreatureReady(player1, new TitaniasChosen());
        Permanent second = addCreatureReady(player2, new TitaniasChosen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Lull(), "{1}{G}");
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castFromHand(player2, new Lull(), "{1}{G}");
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling a green card does not trigger Chosen")
    void cyclingGreenCardDoesNotAddCounter() {
        Permanent chosen = addCreatureReady(player1, new TitaniasChosen());
        harness.setHand(player1, List.of(new Lull()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Lull");
        harness.assertInHand(player1, "Gorilla Warrior");
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
