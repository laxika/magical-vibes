package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlindingDrone;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheFinalWord;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidShatter.class, GrizzlyBears.class, BlindingDrone.class, SphinxOfTheFinalWord.class})
class VoidShatterTest extends BaseCardTest {

    @Test
    void countersAndExilesTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new VoidShatter()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new VoidShatter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotExileAnUncounterableSpell() {
        SphinxOfTheFinalWord sphinx = new SphinxOfTheFinalWord();
        harness.setHand(player1, List.of(sphinx));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setHand(player2, List.of(new VoidShatter()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Void Shatter");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sphinx of the Final Word");
        harness.assertNotInGraveyard(player1, "Sphinx of the Final Word");
    }

    @Test
    void canCounterAndExileItsControllersSpell() {
        BlindingDrone drone = new BlindingDrone();
        harness.setHand(player1, List.of(drone, new VoidShatter()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, drone.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(drone);
        harness.assertNotInGraveyard(player1, "Blinding Drone");
        harness.assertNotOnBattlefield(player1, "Blinding Drone");
        harness.assertInGraveyard(player1, "Void Shatter");
    }

    @Test
    void canCounterAnInstantLeavingItsOriginalTargetToResolve() {
        BlindingDrone drone = new BlindingDrone();
        VoidShatter opposingCounter = new VoidShatter();
        harness.setHand(player1, List.of(drone, new VoidShatter()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(opposingCounter));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, drone.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, opposingCounter.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingCounter);
        harness.assertNotInGraveyard(player2, "Void Shatter");
        harness.assertInGraveyard(player1, "Void Shatter");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Blinding Drone");
    }
}
