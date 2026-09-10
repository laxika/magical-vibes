package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TragicPoet;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FranticSearch.class, TragicPoet.class, TreetopVillage.class})
class FranticSearchTest extends BaseCardTest {

    @Test
    void drawsTwoThenDiscardsTwoBeforeOfferingLandsToUntap() {
        harness.setLibrary(player1, List.of(new TreetopVillage(), new TreetopVillage()));
        addTappedLands(player1, 5);
        harness.addToBattlefield(player1, new TragicPoet());
        tapAllPermanents(player1);
        harness.setHand(player1, List.of(new FranticSearch(), new TragicPoet(), new TragicPoet()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

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
        addTappedLands(player1, 5);
        harness.addToBattlefield(player1, new TragicPoet());
        tapAllPermanents(player1);
        harness.setHand(player1, List.of(new FranticSearch(), new TragicPoet(), new TragicPoet()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treetop Village"))
                .map(Permanent::getId)
                .limit(3)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landIds);

        assertThat(untappedLands(player1)).isEqualTo(3);
        assertThat(findPermanent(player1, "Tragic Poet").isTapped()).isTrue();
    }

    @Test
    void canUntapLandsNotControlledByCaster() {
        addTappedLands(player1, 1);
        addTappedLands(player2, 1);
        Permanent opponentsLand = findPermanent(player2, "Treetop Village");
        harness.setHand(player1, List.of(new FranticSearch(), new TragicPoet(), new TragicPoet()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentsLand.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(opponentsLand.getId()));
        assertThat(opponentsLand.isTapped()).isFalse();
    }

    @Test
    void mayUntapFewerThanThreeLands() {
        addTappedLands(player1, 2);
        harness.setHand(player1, List.of(new FranticSearch(), new TragicPoet(), new TragicPoet()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<UUID> landIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treetop Village"))
                .map(Permanent::getId)
                .limit(1)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landIds);

        assertThat(untappedLands(player1)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseNoLandsToUntap() {
        addTappedLands(player1, 2);
        harness.setHand(player1, List.of(new FranticSearch(), new TragicPoet(), new TragicPoet()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(untappedLands(player1)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addTappedLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new TreetopVillage());
        }
        gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treetop Village"))
                .forEach(Permanent::tap);
    }

    private void tapAllPermanents(Player player) {
        gd.playerBattlefields.get(player.getId()).forEach(Permanent::tap);
    }

    private long untappedLands(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treetop Village"))
                .filter(p -> !p.isTapped())
                .count();
    }
}
