package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzasCommandTest extends BaseCardTest {

    @Test
    @DisplayName("The debuff and Powerstone modes affect only opposing creatures and create a tapped token")
    void debuffsOpposingCreaturesAndCreatesPowerstone() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castWithModes(0, 1);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(0);
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Construct mode scales the token with artifacts you control")
    void constructScalesWithControlledArtifacts() {
        harness.addToBattlefield(player1, new MindStone());

        castWithModes(1, 2);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(construct.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
        assertThat(construct.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The scry mode draws after the scry choice completes")
    void scriesThenDraws() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new MindStone()));

        castWithModes(1, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing fewer than two modes is rejected")
    void rejectsFewerThanTwoModes() {
        harness.setHand(player1, List.of(new UrzasCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode bitmask");
    }

    private void castWithModes(int firstMode, int secondMode) {
        harness.setHand(player1, List.of(new UrzasCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{firstMode, secondMode}, null, List.of());
        harness.passBothPriorities();
    }
}
