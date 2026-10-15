package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.Embargo;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.w.WorryBeads;
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

@CardUsed({CharmedGriffin.class, Embargo.class, FreshVolunteers.class, WorryBeads.class})
class CharmedGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers an artifact or enchantment only to opponents")
    void offersArtifactOrEnchantmentToOpponents() {
        CharmedGriffin griffin = new CharmedGriffin();
        Embargo ownEnchantment = new Embargo();
        WorryBeads opponentArtifact = new WorryBeads();
        Embargo opponentEnchantment = new Embargo();
        FreshVolunteers opponentCreature = new FreshVolunteers();
        harness.setHand(player1, List.of(griffin, ownEnchantment));
        harness.setHand(player2, List.of(opponentArtifact, opponentEnchantment, opponentCreature));

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                (PendingInteraction.EachPlayerMayPutCardFromHandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(opponentArtifact.getId(), opponentEnchantment.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Embargo");
    }

    @Test
    @DisplayName("Opponent may put the chosen artifact onto the battlefield")
    void opponentPutsChosenArtifactOntoBattlefield() {
        CharmedGriffin griffin = new CharmedGriffin();
        WorryBeads opponentArtifact = new WorryBeads();
        harness.setHand(player2, List.of(opponentArtifact));

        harness.castFromHand(player1, griffin, "{3}{W}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of(opponentArtifact.getId()));

        harness.assertOnBattlefield(player2, "Worry Beads");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent may decline to put an eligible card onto the battlefield")
    void opponentMayDecline() {
        CharmedGriffin griffin = new CharmedGriffin();
        WorryBeads opponentArtifact = new WorryBeads();
        harness.setHand(player2, List.of(opponentArtifact));

        harness.castFromHand(player1, griffin, "{3}{W}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of());

        harness.assertNotOnBattlefield(player2, "Worry Beads");
        harness.assertInHand(player2, "Worry Beads");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent may put an enchantment onto the battlefield without paying its mana cost")
    void opponentPutsChosenEnchantmentOntoBattlefield() {
        Embargo enchantment = new Embargo();
        WorryBeads artifact = new WorryBeads();
        harness.setHand(player2, List.of(enchantment, artifact));

        harness.castFromHand(player1, new CharmedGriffin(), "{3}{W}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of(enchantment.getId()));

        harness.assertOnBattlefield(player2, "Embargo");
        harness.assertNotOnBattlefield(player2, "Worry Beads");
        harness.assertInHand(player2, "Worry Beads");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent cannot put more than one eligible card onto the battlefield")
    void opponentCannotChooseTwoCards() {
        Embargo enchantment = new Embargo();
        WorryBeads artifact = new WorryBeads();
        harness.setHand(player2, List.of(enchantment, artifact));

        harness.castFromHand(player1, new CharmedGriffin(), "{3}{W}");
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(enchantment.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player2, "Embargo");
        harness.assertNotOnBattlefield(player2, "Worry Beads");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.handleMultipleCardsChosen(player2, List.of(artifact.getId()));
        harness.assertOnBattlefield(player2, "Worry Beads");
        harness.assertInHand(player2, "Embargo");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB does nothing when an opponent has no artifact or enchantment")
    void doesNothingWithoutEligibleOpponentCard() {
        CharmedGriffin griffin = new CharmedGriffin();
        harness.setHand(player2, List.of(new FreshVolunteers()));

        harness.castFromHand(player1, griffin, "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Fresh Volunteers");
    }
}
