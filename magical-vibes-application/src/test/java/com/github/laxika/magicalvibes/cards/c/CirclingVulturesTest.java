package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.v.Vitalize;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishInfantry.class, CirclingVultures.class, Vitalize.class})
class CirclingVulturesTest extends BaseCardTest {

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    @Test
    @DisplayName("Exiling the top creature card of your graveyard keeps Circling Vultures")
    void payingExilesTopCreatureCard() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Circling Vultures");
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Exiles the top creature card even when a noncreature is above it")
    void payingExilesTopCreatureCardBelowNoncreature() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new Vitalize()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Circling Vultures");
        assertThat(graveyardNames(player1)).containsExactly("Vitalize");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Declining the exile sacrifices Circling Vultures")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Circling Vultures");
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("With no creature card in the graveyard it is sacrificed without a prompt")
    void noCreatureCardSacrifices() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new Vitalize()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Circling Vultures");
        harness.assertInGraveyard(player1, "Vitalize");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new CirclingVultures());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Circling Vultures");
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
