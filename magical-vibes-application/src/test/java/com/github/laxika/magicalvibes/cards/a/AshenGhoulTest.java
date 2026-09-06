package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshenGhoul.class, BalduvianBears.class, Counterspell.class})
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
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(ghoul.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(ghoul.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Returns only the Ashen Ghoul whose ability was activated")
    void returnsOnlyTheActivatedGhoul() {
        AshenGhoul activatedGhoul = new AshenGhoul();
        AshenGhoul otherGhoul = new AshenGhoul();
        harness.setGraveyard(player1, List.of(activatedGhoul, otherGhoul,
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(activatedGhoul.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(otherGhoul.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(otherGhoul.getId()));
    }

    @Test
    @DisplayName("Cannot activate with only two creature cards above it")
    void cannotActivateWithTwoCreaturesAbove() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new BalduvianBears(), new BalduvianBears()));
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
                new Counterspell(), new Counterspell(), new Counterspell()));
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
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears(),
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
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    @DisplayName("Checks the creature threshold when the ability is activated")
    void thresholdIsCheckedWhenActivated() {
        AshenGhoul ghoul = new AshenGhoul();
        harness.setGraveyard(player1, List.of(ghoul,
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of(ghoul,
                new BalduvianBears(), new BalduvianBears()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(ghoul.getId()));
    }
}
