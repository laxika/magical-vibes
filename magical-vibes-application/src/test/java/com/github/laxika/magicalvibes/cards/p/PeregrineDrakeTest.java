package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({PeregrineDrake.class, Forest.class, CoralMerfolk.class})
class PeregrineDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("When Peregrine Drake enters, it offers up to five tapped lands from any battlefield")
    void offersUpToFiveTappedLandsFromAnyBattlefield() {
        List<Permanent> lands = new ArrayList<>();
        lands.addAll(addTappedLands(player1, 3));
        lands.addAll(addTappedLands(player2, 3));

        castPeregrineDrake();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(5);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                lands.stream().map(Permanent::getId).toList());

        List<UUID> chosenIds = choice.validIds().subList(0, 5);
        harness.handleMultiplePermanentsChosen(player1, chosenIds);

        assertThat(lands).filteredOn(land -> chosenIds.contains(land.getId()))
                .allMatch(land -> !land.isTapped());
        assertThat(lands).filteredOn(land -> !chosenIds.contains(land.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Peregrine Drake does not offer non-land permanents to untap")
    void doesNotOfferNonLands() {
        Permanent land = addTappedLands(player2, 1).getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        creature.tap();

        castPeregrineDrake();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Peregrine Drake may untap fewer than five lands")
    void mayUntapFewerThanFiveLands() {
        List<Permanent> lands = addTappedLands(player2, 3);

        castPeregrineDrake();

        harness.handleMultiplePermanentsChosen(player1,
                List.of(lands.get(0).getId(), lands.get(1).getId()));

        assertThat(lands.get(0).isTapped()).isFalse();
        assertThat(lands.get(1).isTapped()).isFalse();
        assertThat(lands.get(2).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Peregrine Drake may choose not to untap any lands")
    void mayUntapNoLands() {
        List<Permanent> lands = addTappedLands(player2, 2);

        castPeregrineDrake();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(lands).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    private void castPeregrineDrake() {
        harness.castFromHand(player1, new PeregrineDrake(), "{4}{U}");
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
