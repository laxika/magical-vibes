package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinisterWaltz.class, GrizzlyBears.class, Shock.class})
class SinisterWaltzTest extends BaseCardTest {

    @Test
    void randomlyReturnsTwoOfThreeTargetsAndPutsTheOtherOnTheBottom() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card libraryCard = new Shock();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SinisterWaltz()));
        addWaltzMana();

        harness.castSorcery(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.minCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        List<UUID> returnedIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard().getId())
                .filter(id -> id.equals(first.getId()) || id.equals(second.getId()) || id.equals(third.getId()))
                .toList();
        assertThat(returnedIds).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(first.getId())
                        || card.getId().equals(second.getId())
                        || card.getId().equals(third.getId()));
        UUID bottomId = List.of(first.getId(), second.getId(), third.getId()).stream()
                .filter(id -> !returnedIds.contains(id))
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(libraryCard.getId(), bottomId);
    }

    @Test
    void returnsAllRemainingLegalTargetsWhenOneTargetLeavesTheGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card libraryCard = new Shock();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SinisterWaltz()));
        addWaltzMana();

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.inMutationScope(() -> gd.playerGraveyards.get(player1.getId()).remove(third));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(libraryCard.getId());
    }

    private void addWaltzMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
