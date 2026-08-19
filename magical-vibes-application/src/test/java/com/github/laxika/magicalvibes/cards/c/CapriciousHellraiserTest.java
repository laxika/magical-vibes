package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapriciousHellraiserTest extends BaseCardTest {

    @Test
    @DisplayName("costs three less to cast with nine cards in the graveyard")
    void costsLessWithNineCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CapriciousHellraiser()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Capricious Hellraiser");
    }

    @Test
    @DisplayName("exiles three random cards, chooses an eligible card, and may cast its copy")
    void choosesEligibleCardAndCastsCopy() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(counsel, bears, forest));
        harness.setHand(player1, List.of(new CapriciousHellraiser()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ExiledSpellCopyChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.ExiledSpellCopyChoice.class);
        assertThat(choice.validCardIds()).containsExactly(counsel.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(counsel, bears, forest);

        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(counsel, bears, forest);
    }

    @Test
    @DisplayName("declining the copy leaves the original selected card exiled")
    void decliningCopyLeavesOriginalExiled() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel, new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new CapriciousHellraiser()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getId().equals(counsel.getId()))
                .hasSize(1);
    }

    @Test
    @DisplayName("does not offer a copy when all three exiled cards are creatures or lands")
    void noEligibleCardMeansNoCopyChoice() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setGraveyard(player1, List.of(bears, firstForest, secondForest));
        harness.setHand(player1, List.of(new CapriciousHellraiser()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(bears, firstForest, secondForest);
    }
}
