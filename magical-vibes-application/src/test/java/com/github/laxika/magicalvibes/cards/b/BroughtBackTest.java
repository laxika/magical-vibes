package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BroughtBackTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two eligible permanents tapped")
    void returnsUpToTwoPermanentsTapped() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent secondBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(bears, secondBears));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondBears);
        });

        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getCard().getId(), secondBears.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId(), secondBears.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(bears.getCard().getId())
                        || p.getCard().getId().equals(secondBears.getCard().getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not return cards that were not put into the graveyard from the battlefield this turn")
    void excludesCardsNotPutThereFromBattlefieldThisTurn() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(bears, shock);
    }
}
