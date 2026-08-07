package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarrowGhoulTest extends BaseCardTest {

    private boolean controlsGhoul(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Barrow Ghoul"));
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    @Test
    @DisplayName("Exiling the top creature card of your graveyard keeps Barrow Ghoul")
    void payingExilesTopCreatureCard() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsGhoul(player1)).isTrue();
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the exile sacrifices Barrow Ghoul")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(controlsGhoul(player1)).isFalse();
        assertThat(graveyardNames(player1)).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("With no creature card in the graveyard the Ghoul is sacrificed without a prompt")
    void noCreatureCardSacrifices() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controlsGhoul(player1)).isFalse();
        assertThat(graveyardNames(player1)).contains("Giant Growth");
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped, not blockers")
    void skipsNoncreatureCardsAboveIt() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GiantGrowth()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsGhoul(player1)).isTrue();
        assertThat(graveyardNames(player1)).containsExactly("Giant Growth");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(controlsGhoul(player1)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
