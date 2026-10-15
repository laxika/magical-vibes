package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacesDefeat.class, JaceBeleren.class, AirElemental.class, GrizzlyBears.class})
class JacesDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a blue Jace planeswalker spell and scries 2")
    void countersJacePlaneswalkerAndScriesTwo() {
        JaceBeleren jace = new JaceBeleren();
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new JacesDefeat()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, jace.getId());

        // Because the countered spell is a Jace planeswalker, it scries 2.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(
                gd, player2, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        // Resolution completes after the scry choice.
        harness.assertInGraveyard(player1, "Jace Beleren");
        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a blue non-Jace spell without scrying")
    void countersBlueNonJaceSpellWithoutScry() {
        AirElemental airElemental = new AirElemental();
        harness.setHand(player1, List.of(airElemental));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setHand(player2, List.of(new JacesDefeat()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, airElemental.getId());

        // Not a Jace planeswalker spell, so no scry — resolution completes in one pass.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a non-blue spell")
    void cannotTargetNonBlueSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new JacesDefeat()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Jace spell is countered before the scry choice")
    void countersJaceBeforeScryChoice() {
        JaceBeleren jace = new JaceBeleren();
        AirElemental first = new AirElemental();
        AirElemental second = new AirElemental();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new JacesDefeat()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, jace.getId());

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Jace Beleren");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(jace.getId()));

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not scry when the targeted Jace spell has already left the stack")
    void doesNotScryWhenTargetHasLeftStack() {
        JaceBeleren jace = new JaceBeleren();
        harness.setLibrary(player2, List.of(new AirElemental(), new AirElemental()));
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new JacesDefeat(), new JacesDefeat()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, jace.getId());
        harness.castAndResolveInstant(player2, 0, jace.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Jace Beleren");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Jace's Defeat"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
