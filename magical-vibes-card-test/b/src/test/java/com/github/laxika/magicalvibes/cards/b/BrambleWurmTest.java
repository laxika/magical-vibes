package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrambleWurm.class})
class BrambleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 5 life")
    void etbGainsFiveLife() {
        harness.setHand(player1, List.of(new BrambleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB → gain 5 life

        harness.assertOnBattlefield(player1, "Bramble Wurm");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Graveyard ability exiles source and gains 5 life")
    void graveyardAbilityExilesAndGainsLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BrambleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Bramble Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bramble Wurm"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Cannot activate graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new BrambleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling is paid immediately, but life is gained only on resolution")
    void exileCostIsPaidBeforeLifeGain() {
        BrambleWurm wurm = new BrambleWurm();
        harness.setGraveyard(player1, List.of(wurm));
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Bramble Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wurm);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 12);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Graveyard activation requires green mana and leaves the card on failed payment")
    void cannotPayGreenRequirementWithColorlessMana() {
        BrambleWurm wurm = new BrambleWurm();
        harness.setGraveyard(player1, List.of(wurm));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Bramble Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(wurm);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Graveyard ability can be activated during the opponent's upkeep")
    void canActivateDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new BrambleWurm()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 12);
        harness.assertNotInGraveyard(player1, "Bramble Wurm");
    }
}
