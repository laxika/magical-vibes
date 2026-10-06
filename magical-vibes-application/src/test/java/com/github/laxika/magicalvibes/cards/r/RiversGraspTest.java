package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiversGrasp.class, Forest.class, GrizzlyBears.class, Peek.class})
class RiversGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Only {U} spent: bounces the target creature, no discard")
    void blueOnlyBouncesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Peek()));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Grizzly Bears")));

        // No discard interaction because {B} was not spent
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Creature bounced to owner's hand; Peek untouched
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Peek");
    }

    @Test
    @DisplayName("Only {B} spent: target player discards a chosen nonland card, no bounce")
    void blackOnlyForcesDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Peek()));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        // No creature target chosen (up to one) — only the mandatory player target.
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        // Peek discarded; creature never bounced ({U} not spent)
        harness.assertInGraveyard(player2, "Peek");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("{U}{B} spent: bounces the creature and forces a discard")
    void bothColorsBounceAndDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Peek()));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Grizzly Bears")));

        // Bounce happens first; revealed hand now holds Peek and the bounced Grizzly Bears.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        // Choose Peek to discard (index 0 was the original hand card).
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("Land cards are not valid discard choices")
    void landsExcludedFromDiscard() {
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, List.of(land, instant));

        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

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

    @Test
    @DisplayName("Both colors: the bounced creature can be chosen for discard")
    void bouncedCreatureCanBeDiscarded() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Grizzly Bears")));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only black spent: a chosen creature target stays on the battlefield")
    void blackOnlyDoesNotBounceChosenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Peek()));
        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("Only blue spent: choosing no creature has no effect on the player's hand")
    void blueOnlyWithNoCreatureTarget() {
        harness.setHand(player2, List.of(new Peek()));
        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        harness.assertInHand(player2, "Peek");
        harness.assertInGraveyard(player1, "River's Grasp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty hand finishes resolution without requiring a discard choice")
    void emptyHandFinishesResolution() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "River's Grasp");
    }

    @Test
    @DisplayName("A land-only hand is revealed without discarding any land")
    void landOnlyHandFinishesResolution() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new RiversGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "River's Grasp");
    }

    @Test
    @DisplayName("The caster may target themselves and choose their own nonland card")
    void casterCanTargetOwnHand() {
        harness.setHand(player1, List.of(new RiversGrasp(), new Peek(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Peek");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
