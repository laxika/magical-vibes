package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InducedAmnesiaTest extends BaseCardTest {

    @Test
    @DisplayName("Target player exiles their hand face down and draws the same number")
    void exilesHandFaceDownAndDrawsThatMany() {
        Card exiledFirst = new GrizzlyBears();
        Card exiledSecond = new LlanowarElves();
        harness.setHand(player2, new ArrayList<>(List.of(exiledFirst, exiledSecond)));
        harness.setHand(player1, new ArrayList<>(List.of(new InducedAmnesia())));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Forest"));
        List<ExiledCardEntry> exiled = gd.exiledCards.stream()
                .filter(entry -> amnesia.getId().equals(entry.sourcePermanentId()))
                .toList();
        assertThat(exiled).hasSize(2)
                .allMatch(entry -> entry.faceDown() && entry.ownerId().equals(player2.getId()));
        assertThat(exiled).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(exiledFirst.getId(), exiledSecond.getId());
    }

    @Test
    @DisplayName("When Induced Amnesia is put into a graveyard, it returns its exiled cards to their owners' hands")
    void returnsExiledCardsWhenPutIntoGraveyard() {
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(exiledCard)));
        harness.setHand(player1, new ArrayList<>(List.of(new InducedAmnesia())));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent amnesia = findPermanent(player1, "Induced Amnesia");
        assertThat(gd.getCardsExiledByPermanent(amnesia.getId())).containsExactly(exiledCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, amnesia));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(exiledCard);
        assertThat(gd.exiledCards).noneMatch(entry -> amnesia.getId().equals(entry.sourcePermanentId()));
    }

}
