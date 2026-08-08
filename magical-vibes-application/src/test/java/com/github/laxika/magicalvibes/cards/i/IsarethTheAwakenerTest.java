package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IsarethTheAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {X} returns a creature card with mana value X with a corpse counter")
    void payingReturnsMatchingCreature() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve the attack trigger -> prompts for X
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities(); // resolve the reflexive return trigger

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A returned creature is exiled instead of going to the graveyard when it dies")
    void returnedCreatureIsExiledInsteadOfDying() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("With several matching creature cards the controller chooses which one returns")
    void controllerChoosesAmongMatchingCreatures() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new ChildOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleGraveyardCardChosen(player1, 1); // Child of Night
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Child of Night")).isNotNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing X=0 declines and returns nothing")
    void decliningReturnsNothing() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("No creature card with the paid mana value means nothing is returned")
    void noMatchingManaValueReturnsNothing() {
        addCreatureReady(player1, new IsarethTheAwakener());
        harness.setGraveyard(player1, List.of(new HillGiant())); // mana value 4
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(countPermanents(player1, "Hill Giant")).isZero();
    }
}
