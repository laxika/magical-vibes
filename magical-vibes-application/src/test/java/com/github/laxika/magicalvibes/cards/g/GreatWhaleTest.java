package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GreatWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("When Great Whale enters, it offers up to seven tapped lands from any battlefield")
    void offersUpToSevenTappedLandsFromAnyBattlefield() {
        List<Permanent> lands = new ArrayList<>();
        lands.addAll(addTappedLands(player1, 4));
        lands.addAll(addTappedLands(player2, 4));

        castGreatWhale();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(7);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                lands.stream().map(Permanent::getId).toList());

        List<UUID> chosenIds = choice.validIds().subList(0, 7);
        harness.handleMultiplePermanentsChosen(player1, chosenIds);

        assertThat(lands).filteredOn(land -> chosenIds.contains(land.getId()))
                .allMatch(land -> !land.isTapped());
        assertThat(lands).filteredOn(land -> !chosenIds.contains(land.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Great Whale does not offer non-land permanents to untap")
    void doesNotOfferNonLands() {
        Permanent land = addTappedLands(player2, 1).getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castGreatWhale();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(land.getId());
    }

    private void castGreatWhale() {
        harness.setHand(player1, List.of(new GreatWhale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> addTappedLands(Player player, int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
            land.tap();
            lands.add(land);
        }
        return lands;
    }
}
