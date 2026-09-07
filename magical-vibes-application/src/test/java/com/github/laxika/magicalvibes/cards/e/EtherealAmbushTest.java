package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EtherealAmbushTest extends BaseCardTest {

    @Test
    void manifestsTheTopTwoCards() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        Card remainingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of(firstCard, secondCard, remainingCard));
        addEtherealAmbushMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .toList();
        assertThat(manifested).hasSize(2);
        assertThat(manifested).allMatch(Permanent::isFaceDown);
        assertThat(manifested)
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstCard.getId(), secondCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void manifestsOnlyAvailableCard() {
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of(topCard));
        addEtherealAmbushMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .map(permanent -> permanent.getCard().getId()))
                .containsExactly(topCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNothingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of());
        addEtherealAmbushMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    private void addEtherealAmbushMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
