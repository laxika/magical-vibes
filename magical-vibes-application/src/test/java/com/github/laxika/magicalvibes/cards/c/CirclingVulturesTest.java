package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CirclingVulturesTest extends BaseCardTest {

    private boolean controlsVultures(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Circling Vultures"));
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    @Test
    @DisplayName("Exiling the top creature card of your graveyard keeps Circling Vultures")
    void payingExilesTopCreatureCard() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsVultures(player1)).isTrue();
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the exile sacrifices Circling Vultures")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(controlsVultures(player1)).isFalse();
        assertThat(graveyardNames(player1)).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("With no creature card in the graveyard it is sacrificed without a prompt")
    void noCreatureCardSacrifices() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controlsVultures(player1)).isFalse();
        assertThat(graveyardNames(player1)).contains("Giant Growth");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(controlsVultures(player1)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The hand ability discards it from hand for free")
    void handAbilityDiscardsItself() {
        harness.setHand(player1, List.of(new CirclingVultures()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Circling Vultures");
    }

    @Test
    @DisplayName("The hand ability can be used during the opponent's turn")
    void handAbilityUsableOnOpponentTurn() {
        harness.setHand(player1, List.of(new CirclingVultures()));
        harness.forceActivePlayer(player2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(graveyardNames(player1)).containsExactly("Circling Vultures");
    }
}
