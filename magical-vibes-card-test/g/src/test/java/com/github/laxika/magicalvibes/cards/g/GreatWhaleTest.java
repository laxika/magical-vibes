package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GreatWhale.class, Forest.class, CoralMerfolk.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        creature.tap();

        castGreatWhale();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Great Whale lets its controller choose not to untap any lands")
    void canChooseNoLands() {
        Permanent land = addTappedLands(player2, 1).getFirst();

        castGreatWhale();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Great Whale can untap fewer than seven lands while leaving the rest tapped")
    void canChooseFewerThanSevenLands() {
        List<Permanent> lands = addTappedLands(player1, 4);

        castGreatWhale();
        harness.handleMultiplePermanentsChosen(player1, List.of(lands.getFirst().getId()));

        assertThat(lands.getFirst().isTapped()).isFalse();
        assertThat(lands.subList(1, lands.size())).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Great Whale resolves normally with no lands on the battlefield")
    void resolvesWithNoLands() {
        castGreatWhale();

        harness.assertOnBattlefield(player1, "Great Whale");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Great Whale resolves normally when all lands are already untapped")
    void resolvesWithOnlyUntappedLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castGreatWhale();

        assertThat(land.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Great Whale");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    private void castGreatWhale() {
        harness.castFromHand(player1, new GreatWhale(), "{5}{U}{U}");
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
