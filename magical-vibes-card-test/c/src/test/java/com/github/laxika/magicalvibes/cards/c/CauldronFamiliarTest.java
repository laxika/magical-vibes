package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FortifyingProvisions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CauldronFamiliar.class, FortifyingProvisions.class})
class CauldronFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and drains one life")
    void entersAndDrainsOneLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CauldronFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing Food returns it from the graveyard and triggers its drain")
    void sacrificingFoodReturnsItAndTriggersItsDrain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        CauldronFamiliar familiar = new CauldronFamiliar();
        harness.setGraveyard(player1, List.of(familiar));
        createFood();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertOnBattlefield(player1, "Cauldron Familiar");
        harness.assertNotInGraveyard(player1, "Cauldron Familiar");
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot activate without a Food to sacrifice")
    void cannotActivateWithoutFood() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CauldronFamiliar()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void createFood() {
        harness.setHand(player1, List.of(new FortifyingProvisions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void returnsOnlyTheFamiliarWhoseAbilityWasActivated() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        CauldronFamiliar first = new CauldronFamiliar();
        CauldronFamiliar second = new CauldronFamiliar();
        harness.setGraveyard(player1, List.of(first, second));
        createFood();

        harness.activateGraveyardAbility(player1, 1);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertNotOnBattlefield(player1, "Cauldron Familiar");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(second.getId()));
        assertThat(countPermanents(player1, "Cauldron Familiar")).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void canReturnDuringOpponentsTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new CauldronFamiliar()));
        createFood();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cauldron Familiar");
        harness.assertNotInGraveyard(player1, "Cauldron Familiar");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void cannotSacrificeOpponentsFood() {
        harness.setGraveyard(player2, List.of(new CauldronFamiliar()));
        createFood();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertInGraveyard(player2, "Cauldron Familiar");
        harness.assertNotOnBattlefield(player2, "Cauldron Familiar");
    }
}
