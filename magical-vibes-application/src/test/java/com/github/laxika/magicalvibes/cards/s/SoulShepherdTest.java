package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulShepherd.class, BenalishKnight.class, MindStone.class, SerratedBiskelion.class})
class SoulShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles a creature card from graveyard and gains 1 life")
    void abilityExilesCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Benalish Knight");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Benalish Knight"));

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Ability prompts for the graveyard exile cost choice")
    void abilityPromptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new MindStone(), new BenalishKnight(), new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1, 2);
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
        harness.setGraveyard(player1, List.of(new BenalishKnight()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Ability rejects a noncreature card as the graveyard cost")
    void abilityRejectsNonCreatureCardInGraveyard() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Ability cannot use a creature card from an opponent's graveyard")
    void abilityCannotUseOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Ability can exile an artifact creature card from the graveyard")
    void abilityCanExileArtifactCreatureCard() {
        harness.addToBattlefield(player1, new SoulShepherd());
        harness.setGraveyard(player1, List.of(new SerratedBiskelion()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Serrated Biskelion"));
    }
}
