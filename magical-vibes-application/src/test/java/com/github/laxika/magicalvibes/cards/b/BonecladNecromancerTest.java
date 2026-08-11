package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BonecladNecromancerTest extends BaseCardTest {

    private void castNecromancer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonecladNecromancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the ETB ability exiles a creature card and creates a Zombie")
    void exilesCreatureAndCreatesZombie() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        castNecromancer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertOnBattlefield(player1, "Zombie");
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the graveyard unchanged")
    void decliningLeavesGraveyardUnchanged() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castNecromancer();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A noncreature card cannot be chosen for the ETB ability")
    void noncreatureCardCannotBeChosen() {
        harness.setGraveyard(player2, List.of(new Shock()));

        castNecromancer();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }
}
