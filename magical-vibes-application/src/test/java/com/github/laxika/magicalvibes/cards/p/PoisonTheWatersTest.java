package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoisonTheWaters.class, Forest.class, GrizzlyBears.class, Millstone.class, Peek.class})
class PoisonTheWatersTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives all creatures -1/-1")
    void shrinksAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMode(0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castMode(0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The second mode discards a chosen artifact or creature from the target player's hand")
    void discardsChosenArtifactOrCreature() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Millstone(), new Peek(), new GrizzlyBears())));
        castMode(1, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1, 3);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Millstone");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Peek", "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode does nothing when the target hand has no artifact or creature")
    void noMatchingCardNeedsNoChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek())));
        castMode(1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    private void castMode(int mode, java.util.UUID... targetIds) {
        harness.setHand(player1, List.of(new PoisonTheWaters()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetIds.length == 0) {
            harness.castSorcery(player1, 0, mode);
        } else {
            harness.castSorcery(player1, 0, mode, targetIds[0]);
        }
        harness.passBothPriorities();
    }
}
