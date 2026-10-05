package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OathswornVampire.class, MomentOfCraving.class})
class OathswornVampireTest extends BaseCardTest {

    private void prepareGraveyardCast() {
        harness.setGraveyard(player1, List.of(new OathswornVampire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard unless you gained life this turn")
    void cannotCastFromGraveyardWithoutLifeGain() {
        prepareGraveyardCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Can be cast from the graveyard after gaining life this turn")
    void canCastFromGraveyardAfterGainingLife() {
        prepareGraveyardCast();
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent vampire = findPermanent(player1, "Oathsworn Vampire");
        assertThat(vampire).isNotNull();
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's life gain does not enable the graveyard cast")
    void opponentLifeGainDoesNotEnableGraveyardCast() {
        prepareGraveyardCast();
        gd.lifeGainedThisTurn.put(player2.getId(), 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }
    @Test
    @DisplayName("Casting from hand does not require life gain and enters tapped")
    void canCastFromHandWithoutLifeGain() {
        harness.castFromHand(player1, new OathswornVampire(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Oathsworn Vampire").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gained before entering the graveyard enables casting even after losing more life")
    void lifeGainBeforeDeathAndSubsequentLifeLossStillEnablesCast() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new OathswornVampire());
        harness.setHand(player1, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, vampire.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Oathsworn Vampire");
        harness.setLife(player1, 15);

        int vampireIndex = gd.playerGraveyards.get(player1.getId()).indexOf(vampire.getCard());
        harness.castFromGraveyard(player1, vampireIndex);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Oathsworn Vampire");
        assertThat(findPermanent(player1, "Oathsworn Vampire").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain does not waive the normal mana cost")
    void cannotCastFromGraveyardWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new OathswornVampire()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Oathsworn Vampire");
        harness.assertNotOnBattlefield(player1, "Oathsworn Vampire");
    }

    @Test
    @DisplayName("Life gain does not allow casting outside a main phase")
    void cannotCastFromGraveyardDuringCombat() {
        prepareGraveyardCast();
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from graveyard now");
        harness.assertInGraveyard(player1, "Oathsworn Vampire");
    }
}
