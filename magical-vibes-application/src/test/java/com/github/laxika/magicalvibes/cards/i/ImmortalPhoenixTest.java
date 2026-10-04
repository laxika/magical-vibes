package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImmortalPhoenix.class, WrathOfGod.class})
class ImmortalPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("When Immortal Phoenix dies, it returns to its owner's hand")
    void diesReturnsToOwnersHand() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new ImmortalPhoenix());
        Card phoenixCard = phoenix.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenixCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenixCard.getId()));
    }

    @Test
    @DisplayName("Phoenix stays in the graveyard until its death trigger resolves")
    void returnUsesTheStack() {
        ImmortalPhoenix phoenix = new ImmortalPhoenix();
        harness.addToBattlefield(player1, phoenix);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(phoenix);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(phoenix);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(phoenix);
    }

    @Test
    @DisplayName("An opponent-controlled Phoenix returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        ImmortalPhoenix phoenix = new ImmortalPhoenix();
        phoenix.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, phoenix);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(phoenix);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(phoenix);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(phoenix);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(phoenix);
    }

    @Test
    @DisplayName("Simultaneously dying Phoenixes each return their own card")
    void simultaneousDeathsReturnEachPhoenix() {
        ImmortalPhoenix first = new ImmortalPhoenix();
        ImmortalPhoenix second = new ImmortalPhoenix();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player2, second);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerHands.get(player2.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(second);
    }
}
