package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatronOfTheVein.class, GrizzlyBears.class, Shock.class, VampireNoble.class})
class PatronOfTheVeinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys target creature an opponent controls")
    void entersAndDestroysTargetOpponentCreature() {
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PatronOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, victim.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its own controller")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PatronOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Exiles a dying opponent creature and puts counters on each Vampire controlled")
    void exilesDyingOpponentCreatureAndCountersVampires() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheVein());
        Permanent vampire = addCreatureReady(player1, new VampireNoble());
        Permanent nonVampire = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(victim.getCard());
        assertThat(patron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature destroyed by the enter trigger also triggers exile and Vampire counters")
    void enterTriggerDestructionTriggersDeathAbility() {
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PatronOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, victim.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(victim.getCard());
        assertThat(findPermanent(player1, "Patron of the Vein")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Patron can enter when no opponent controls a creature")
    void entersWithoutAnOpponentCreature() {
        harness.setHand(player1, List.of(new PatronOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Patron of the Vein");
        assertThat(findPermanent(player1, "Patron of the Vein")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Patron grants counters even after another Patron exiles the dying creature")
    void multiplePatronsEachGrantCounters() {
        Permanent firstPatron = addCreatureReady(player1, new PatronOfTheVein());
        Permanent secondPatron = addCreatureReady(player1, new PatronOfTheVein());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(victim.getCard());
        assertThat(firstPatron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondPatron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature controlled by Patron's controller dies normally without triggering Patron")
    void ownCreatureDeathDoesNotTrigger() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheVein());
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(victim.getCard());
        assertThat(patron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death trigger does not put counters on an opponent's Vampires")
    void opponentVampiresDoNotReceiveCounters() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheVein());
        Permanent opposingPatron = addCreatureReady(player2, new PatronOfTheVein());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(victim.getCard());
        assertThat(patron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingPatron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
