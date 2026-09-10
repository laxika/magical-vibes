package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudOfFaeries.class, FaerieConclave.class, GiantCockroach.class})
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
    @DisplayName("When Cloud of Faeries enters, its controller may choose fewer than two lands")
    void mayChooseFewerThanTwoLands() {
        List<Permanent> lands = addTappedLands(player1, 2);

        castCloudOfFaeries();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(lands.getFirst().getId()));

        assertThat(lands.getFirst().isTapped()).isFalse();
        assertThat(lands.get(1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When no lands are tapped, Cloud of Faeries resolves without a choice")
    void resolvesWithoutTappedLands() {
        castCloudOfFaeries();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cloud of Faeries");
    }

    @Test
    @DisplayName("Cloud of Faeries does not offer non-land permanents to untap")
    void doesNotOfferNonLands() {
        Permanent land = addTappedLands(player2, 1).getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
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
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cloud of Faeries");
        harness.assertInHand(player1, "Giant Cockroach");
    }

    private void castCloudOfFaeries() {
        harness.castFromHand(player1, new CloudOfFaeries(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> addTappedLands(Player player, int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player, new FaerieConclave());
            land.tap();
            lands.add(land);
        }
        return lands;
    }
}
