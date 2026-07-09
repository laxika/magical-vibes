package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScarredVinebreederTest extends BaseCardTest {

    private Permanent setup(List<Card> graveyard, int manaAvailable) {
        Permanent vinebreeder = harness.addToBattlefieldAndReturn(player1, new ScarredVinebreeder());
        vinebreeder.setSummoningSick(false);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, manaAvailable);
        return vinebreeder;
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Activating prompts to choose an Elf card to exile")
    void promptsForElfExile() {
        Permanent vinebreeder = setup(List.of(new LlanowarElves()), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Only Elf cards are valid to exile as the cost")
    void onlyElfCardsAreValid() {
        // Graveyard: index 0 non-Elf (Grizzly Bears), index 1 Elf (Llanowar Elves)
        Permanent vinebreeder = setup(List.of(new GrizzlyBears(), new LlanowarElves()), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Exiles the chosen Elf, pumps +3/+3, and consumes {2}{B}")
    void exilesElfAndPumps() {
        Permanent vinebreeder = setup(List.of(new LlanowarElves()), 4);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(3);
        // 4 - 3 ({2}{B}) = 1 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The +3/+3 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vinebreeder = setup(List.of(new LlanowarElves()), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);

        advanceToNextTurn(player1);

        assertThat(vinebreeder.getPowerModifier()).isZero();
        assertThat(vinebreeder.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without an Elf card in graveyard")
    void cannotActivateWithoutElfInGraveyard() {
        Permanent vinebreeder = setup(List.of(new GrizzlyBears()), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(vinebreeder), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent vinebreeder = setup(List.of(new LlanowarElves()), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(vinebreeder), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
