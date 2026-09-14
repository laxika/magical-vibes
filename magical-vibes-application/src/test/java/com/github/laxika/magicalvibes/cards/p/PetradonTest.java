package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChainersEdict;
import com.github.laxika.magicalvibes.cards.t.TaintedPeak;
import com.github.laxika.magicalvibes.cards.t.TaintedWood;
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

@CardUsed({Petradon.class, ChainersEdict.class, TaintedPeak.class, TaintedWood.class})
class PetradonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles two target lands and tracks them with Petradon")
    void etbExilesTwoTargetLands() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        Permanent wood = harness.addToBattlefieldAndReturn(player2, new TaintedWood());

        Permanent petradon = castAndResolvePetradon(peak, wood);

        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        harness.assertNotOnBattlefield(player2, "Tainted Wood");
        assertThat(gd.getCardsExiledByPermanent(petradon.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Tainted Peak", "Tainted Wood");
    }

    @Test
    @DisplayName("ETB cannot target a nonland permanent")
    void etbCannotTargetNonlandPermanent() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        Permanent wood = harness.addToBattlefieldAndReturn(player2, new TaintedWood());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Petradon());

        castPetradonSpell();
        harness.handlePermanentChosen(player1, peak.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, wood.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        harness.assertNotOnBattlefield(player2, "Tainted Wood");
        harness.assertOnBattlefield(player2, "Petradon");
    }

    @Test
    @DisplayName("ETB cannot target the same land twice")
    void etbCannotTargetSameLandTwice() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        Permanent wood = harness.addToBattlefieldAndReturn(player2, new TaintedWood());

        castPetradonSpell();
        harness.handlePermanentChosen(player1, peak.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, peak.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, wood.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        harness.assertNotOnBattlefield(player2, "Tainted Wood");
    }

    @Test
    @DisplayName("ETB still exiles the remaining land when one target leaves before resolution")
    void etbExilesRemainingLandWhenOneTargetLeavesBeforeResolution() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        Permanent wood = harness.addToBattlefieldAndReturn(player2, new TaintedWood());

        castPetradonSpell();
        harness.handlePermanentChosen(player1, peak.getId());
        harness.handlePermanentChosen(player1, wood.getId());
        Permanent petradon = findPermanent(player1, "Petradon");

        gd.playerBattlefields.get(player2.getId()).remove(peak);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(petradon.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Tainted Wood");
        harness.assertNotOnBattlefield(player2, "Tainted Wood");
    }

    @Test
    @DisplayName("The exiled lands return under their owners' control when Petradon leaves")
    void exiledLandsReturnWhenPetradonLeaves() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        Permanent wood = harness.addToBattlefieldAndReturn(player2, new TaintedWood());
        Permanent petradon = castAndResolvePetradon(peak, wood);

        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petradon");
        harness.assertOnBattlefield(player2, "Tainted Peak");
        harness.assertOnBattlefield(player2, "Tainted Wood");
        assertThat(gd.getCardsExiledByPermanent(petradon.getId())).isEmpty();
    }

    @Test
    @DisplayName("Red mana gives Petradon +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent petradon = addReadyPetradon();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(petradon.getPowerModifier()).isEqualTo(1);
        assertThat(petradon.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(petradon.getPowerModifier()).isZero();
        assertThat(petradon.getToughnessModifier()).isZero();
    }

    private Permanent castAndResolvePetradon(Permanent firstLand, Permanent secondLand) {
        castPetradonSpell();
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        return findPermanent(player1, "Petradon");
    }

    private void castPetradonSpell() {
        harness.setHand(player1, List.of(new Petradon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyPetradon() {
        return addCreatureReady(player1, new Petradon());
    }
}
