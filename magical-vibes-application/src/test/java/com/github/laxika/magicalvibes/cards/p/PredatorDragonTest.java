package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.h.HissingIguanar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatorDragon.class, CylianElf.class, HissingIguanar.class})
class PredatorDragonTest extends BaseCardTest {

    private void castDragon() {
        harness.castFromHand(player1, new PredatorDragon(), "{3}{R}{R}{R}");
    }

    private Permanent dragon() {
        return findPermanent(player1, "Predator Dragon");
    }

    @Test
    @DisplayName("Devouring two creatures gives four +1/+1 counters (Devour 2)")
    void devourTwoAddsFourCounters() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        castDragon();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new CylianElf());

        castDragon();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no other creatures, enters with no counters and no prompt")
    void noOtherCreaturesNoPrompt() {
        castDragon();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt)

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Devour can sacrifice just one creature while leaving other creatures alone")
    void devourOnlySelectedCreature() {
        Permanent selected = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CylianElf());

        castDragon();
        harness.passBothPriorities();

        var choice = (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(selected.getId(), unselected.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unselected).doesNotContain(selected);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        harness.assertInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Devour applies when the Dragon enters without being cast")
    void devourOnNoncastEntry() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new PredatorDragon());
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        harness.assertInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("A devoured Iguanar sees the other creature die in the same sacrifice event")
    void devouredWatcherSeesSimultaneousDeath() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new HissingIguanar());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        int lifeBefore = gd.getLife(player2.getId());

        castDragon();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(iguanar.getId(), fodder.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Hissing Iguanar");
        harness.assertInGraveyard(player1, "Cylian Elf");
    }
}
