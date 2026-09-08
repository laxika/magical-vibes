package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class CloudOfFaeriesTest extends BaseCardTest {

    @Test
    @DisplayName("When Cloud of Faeries enters, it offers up to two tapped lands from any battlefield")
    void offersUpToTwoTappedLandsFromAnyBattlefield() {
        List<Permanent> lands = new ArrayList<>();
        lands.addAll(addTappedLands(player1, 2));
        lands.addAll(addTappedLands(player2, 2));

        castCloudOfFaeries();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                lands.stream().map(Permanent::getId).toList());

        List<UUID> chosenIds = choice.validIds().subList(0, 2);
        harness.handleMultiplePermanentsChosen(player1, chosenIds);

        assertThat(lands).filteredOn(land -> chosenIds.contains(land.getId()))
                .allMatch(land -> !land.isTapped());
        assertThat(lands).filteredOn(land -> !chosenIds.contains(land.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cloud of Faeries does not offer non-land permanents to untap")
    void doesNotOfferNonLands() {
        Permanent land = addTappedLands(player2, 1).getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castCloudOfFaeries();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Cycling discards Cloud of Faeries and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CloudOfFaeries()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cloud of Faeries");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castCloudOfFaeries() {
        harness.setHand(player1, List.of(new CloudOfFaeries()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
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
