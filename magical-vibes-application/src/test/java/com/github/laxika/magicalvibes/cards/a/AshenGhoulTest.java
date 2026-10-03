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
    @DisplayName("Cannot return without paying black mana")
    void cannotActivateWithoutBlackMana() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupUpkeep();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ashen Ghoul");
        harness.assertNotOnBattlefield(player1, "Ashen Ghoul");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature cards need not be consecutive or immediately above Ashen Ghoul")
    void returnsWithInterspersedNonCreatureCards() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(), new Counterspell(),
                new BalduvianBears(), new Counterspell(), new BalduvianBears(),
                new Counterspell(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ashen Ghoul");
        harness.assertNotInGraveyard(player1, "Ashen Ghoul");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Can activate twice while still in the graveyard but returns only once")
    void multipleActivationsReturnOnlyOnce() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Ashen Ghoul");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Does not return another Ashen Ghoul if the source leaves before resolution")
    void missingSourceDoesNotReturnAnotherGhoul() {
        AshenGhoul ghoul = new AshenGhoul();
        AshenGhoul otherGhoul = new AshenGhoul();
        harness.setGraveyard(player1, List.of(ghoul, otherGhoul,
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of(otherGhoul,
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.setHand(player1, List.of(ghoul));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashen Ghoul");
        harness.assertInHand(player1, "Ashen Ghoul");
        harness.assertInGraveyard(player1, "Ashen Ghoul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can attack the turn it returns from the graveyard")
    void canAttackAfterReturning() {
        harness.setGraveyard(player1, List.of(new AshenGhoul(),
                new BalduvianBears(), new BalduvianBears(), new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Ashen Ghoul").isTapped()).isTrue();
        harness.assertLife(player2, 17);
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
