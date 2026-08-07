package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles a creature card from graveyard and gains 1 life")
    void abilityExilesCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Ability prompts for the graveyard exile cost choice")
    void abilityPromptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Ability fails with no creature card in graveyard")
    void abilityFailsWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Ability fails without white mana")
    void abilityFailsWithoutMana() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }
}
