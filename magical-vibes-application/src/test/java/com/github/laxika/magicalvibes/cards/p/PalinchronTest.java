package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
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

@CardUsed({Palinchron.class, TreetopVillage.class, GiantCockroach.class})
class PalinchronTest extends BaseCardTest {

    @Test
    @DisplayName("When Palinchron enters, it offers up to seven tapped lands from any battlefield")
    void offersUpToSevenTappedLandsFromAnyBattlefield() {
        List<Permanent> lands = new ArrayList<>();
        lands.addAll(addTappedLands(player1, 4));
        lands.addAll(addTappedLands(player2, 4));

        castPalinchron();

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
    @DisplayName("Palinchron does not offer non-land permanents to untap")
    void doesNotOfferNonLands() {
        Permanent land = addTappedLands(player2, 1).getFirst();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        creature.tap();

        castPalinchron();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Palinchron may untap fewer than seven lands")
    void mayUntapFewerThanSevenLands() {
        Permanent land = addTappedLands(player1, 1).getFirst();

        castPalinchron();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activateAbilityReturnsPalinchronToItsOwnersHand() {
        harness.addToBattlefield(player1, new Palinchron());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Palinchron");
        harness.assertNotOnBattlefield(player1, "Palinchron");
    }

    private void castPalinchron() {
        harness.castFromHand(player1, new Palinchron(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> addTappedLands(Player player, int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player, new TreetopVillage());
            land.tap();
            lands.add(land);
        }
        return lands;
    }
}
