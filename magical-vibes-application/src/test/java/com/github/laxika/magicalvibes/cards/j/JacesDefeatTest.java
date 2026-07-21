package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castInstant(player2, 0, jace.getId());
        harness.passBothPriorities(); // Jace's Defeat resolves, pausing on the scry

        // Because the countered spell is a Jace planeswalker, it scries 2.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(
                gd, player2, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        // Resolution resumes and the Jace planeswalker is countered.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jace Beleren"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Jace Beleren"));
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
        harness.castInstant(player2, 0, airElemental.getId());
        harness.passBothPriorities();

        // Not a Jace planeswalker spell, so no scry — resolution completes in one pass.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
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
}
