package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CharmedGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers an artifact or enchantment only to opponents")
    void offersArtifactOrEnchantmentToOpponents() {
        CharmedGriffin griffin = new CharmedGriffin();
        GloriousAnthem ownEnchantment = new GloriousAnthem();
        HowlingMine opponentArtifact = new HowlingMine();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(griffin, ownEnchantment));
        harness.setHand(player2, List.of(opponentArtifact, opponentCreature));

        castGriffin();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                (PendingInteraction.EachPlayerMayPutCardFromHandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(opponentArtifact.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Glorious Anthem");
    }

    @Test
    @DisplayName("Opponent may put the chosen artifact onto the battlefield")
    void opponentPutsChosenArtifactOntoBattlefield() {
        CharmedGriffin griffin = new CharmedGriffin();
        HowlingMine opponentArtifact = new HowlingMine();
        harness.setHand(player1, List.of(griffin));
        harness.setHand(player2, List.of(opponentArtifact));

        castGriffin();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(opponentArtifact.getId()));

        harness.assertOnBattlefield(player2, "Howling Mine");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB does nothing when an opponent has no artifact or enchantment")
    void doesNothingWithoutEligibleOpponentCard() {
        harness.setHand(player1, List.of(new CharmedGriffin()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        castGriffin();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void castGriffin() {
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
    }
}
