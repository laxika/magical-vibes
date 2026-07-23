package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshenGhoulTest extends BaseCardTest {

    private void setupUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
    }

    @Test
    @DisplayName("Returns from graveyard during upkeep with three creatures above it")
    void returnsWithThreeCreaturesAbove() {
        AshenGhoul ghoul = new AshenGhoul();
        // Bottom to top: Ashen Ghoul first, then three creatures above it.
        harness.setGraveyard(player1, List.of(ghoul,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(ghoul.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ghoul.getId()));
    }

    @Test
    @DisplayName("Cannot activate with only two creature cards above it")
    void cannotActivateWithTwoCreaturesAbove() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more creature cards are above this card");
    }

    @Test
    @DisplayName("Non-creature cards above it do not count toward the threshold")
    void nonCreatureCardsAboveDoNotCount() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more creature cards are above this card");
    }

    @Test
    @DisplayName("Creature cards below it in the graveyard do not count")
    void creaturesBelowDoNotCount() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new AshenGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more creature cards are above this card");
    }

    @Test
    @DisplayName("Cannot activate outside of your upkeep")
    void cannotActivateOutsideUpkeep() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }
}
