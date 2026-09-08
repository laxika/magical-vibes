package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueenKaylaBinKroogTest extends BaseCardTest {

    @Test
    void discardsDrawsAndReturnsOneEligibleCardAtEachManaValue() {
        Card manaValueOneCreature = new LlanowarElves();
        Card manaValueTwoCreature = new GrizzlyBears();
        Card manaValueThreeArtifact = new Manalith();
        Card ineligibleCard = new GiantGrowth();
        QueenKaylaBinKroog queen = new QueenKaylaBinKroog();

        addCreatureReady(player1, queen);
        harness.setHand(player1, List.of(manaValueOneCreature, manaValueTwoCreature,
                manaValueThreeArtifact, ineligibleCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        choose(player1, manaValueOneCreature);
        choose(player1, manaValueTwoCreature);
        choose(player1, manaValueThreeArtifact);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(manaValueOneCreature.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(manaValueTwoCreature.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(manaValueThreeArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ineligibleCard.getId()))
                .noneMatch(card -> card.getId().equals(manaValueOneCreature.getId()))
                .noneMatch(card -> card.getId().equals(manaValueTwoCreature.getId()))
                .noneMatch(card -> card.getId().equals(manaValueThreeArtifact.getId()));
    }

    private void choose(com.github.laxika.magicalvibes.model.Player player, Card card) {
        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.GraveyardChoice.class);
        int index = java.util.stream.IntStream.range(0, choice.cardPool().size())
                .filter(i -> choice.cardPool().get(i).getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player, index);
    }
}
