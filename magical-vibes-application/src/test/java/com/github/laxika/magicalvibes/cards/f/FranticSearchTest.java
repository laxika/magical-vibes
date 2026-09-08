package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FranticSearchTest extends BaseCardTest {

    @Test
    void drawsTwoThenDiscardsTwoBeforeOfferingLandsToUntap() {
        setDeck(player1, List.of(new Island(), new Island()));
        addTappedIslands(player1, 5);
        harness.addToBattlefield(player1, new GrizzlyBears());
        tapAllPermanents(player1);
        harness.setHand(player1, List.of(new FranticSearch(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds()).hasSize(5);
    }

    @Test
    void untapsUpToThreeLandsAndLeavesOtherPermanentsTapped() {
        addTappedIslands(player1, 5);
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        tapAllPermanents(player1);
        harness.setHand(player1, List.of(new FranticSearch(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .map(Permanent::getId)
                .limit(3)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landIds);

        assertThat(untappedIslands(player1)).isEqualTo(3);
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    void mayUntapFewerThanThreeLands() {
        addTappedIslands(player1, 2);
        harness.setHand(player1, List.of(new FranticSearch(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .map(Permanent::getId)
                .limit(1)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landIds);

        assertThat(untappedIslands(player1)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addTappedIslands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Island());
        }
        gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .forEach(Permanent::tap);
    }

    private void tapAllPermanents(Player player) {
        gd.playerBattlefields.get(player.getId()).forEach(Permanent::tap);
    }

    private long untappedIslands(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(p -> !p.isTapped())
                .count();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
