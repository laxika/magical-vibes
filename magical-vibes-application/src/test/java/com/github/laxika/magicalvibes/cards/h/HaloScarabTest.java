package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HaloScarab.class)
class HaloScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles Halo Scarab and creates a Treasure")
    void graveyardAbilityExilesSourceAndCreatesTreasure() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new HaloScarab()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Halo Scarab");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Halo Scarab"));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate Halo Scarab's graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new HaloScarab()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Costs are paid immediately but the Treasure is created only on resolution")
    void paysCostsBeforeCreatingTreasure() {
        harness.setGraveyard(player1, List.of(new HaloScarab()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Halo Scarab");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Graveyard ability can be activated during an opponent's upkeep with colored mana")
    void canActivateDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new HaloScarab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertNotInGraveyard(player1, "Halo Scarab");
    }

    @Test
    @DisplayName("An unaffordable activation leaves Halo Scarab and the mana untouched")
    void failedActivationDoesNotPayCosts() {
        harness.setGraveyard(player1, List.of(new HaloScarab()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Halo Scarab");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
