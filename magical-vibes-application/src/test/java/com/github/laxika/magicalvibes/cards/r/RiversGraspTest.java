package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiversGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Only {U} spent: bounces the target creature, no discard")
    void blueOnlyBouncesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        // No discard interaction because {B} was not spent
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Creature bounced to owner's hand; Peek untouched
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Only {B} spent: target player discards a chosen nonland card, no bounce")
    void blackOnlyForcesDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        // No creature target chosen (up to one) — only the mandatory player target.
        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        // Peek discarded; creature never bounced ({U} not spent)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("{U}{B} spent: bounces the creature and forces a discard")
    void bothColorsBounceAndDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        // Bounce happens first; revealed hand now holds Peek and the bounced Grizzly Bears.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        // Choose Peek to discard (index 0 was the original hand card).
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Land cards are not valid discard choices")
    void landsExcludedFromDiscard() {
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(land, instant)));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("The creature target must be a creature")
    void creatureTargetMustBeACreature() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Forest"))))
                .isInstanceOf(IllegalStateException.class);
    }
}
