package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MoonBoyDinoRider.class, FrenziedRaptor.class, GrizzlyBears.class})
class MoonBoyDinoRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur spells cost {1} less to cast")
    void dinosaurSpellsCostOneLess() {
        harness.addToBattlefield(player1, new MoonBoyDinoRider());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Dinosaur spells are not reduced")
    void nonDinosaurSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new MoonBoyDinoRider());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking while its controller controls a Dinosaur")
    void boostsOnAttackWithDinosaur() {
        Permanent moonBoy = addCreatureReady(player1, new MoonBoyDinoRider());
        addCreatureReady(player1, new FrenziedRaptor());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(moonBoy.getPowerModifier()).isEqualTo(1);
        assertThat(moonBoy.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a boost without a Dinosaur")
    void doesNotBoostWithoutDinosaur() {
        Permanent moonBoy = addCreatureReady(player1, new MoonBoyDinoRider());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(moonBoy.getPowerModifier()).isZero();
        assertThat(moonBoy.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Dinosaur does not enable the boost")
    void opponentDinosaurDoesNotCount() {
        Permanent moonBoy = addCreatureReady(player1, new MoonBoyDinoRider());
        addCreatureReady(player2, new FrenziedRaptor());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(moonBoy.getPowerModifier()).isZero();
        assertThat(moonBoy.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent moonBoy = addCreatureReady(player1, new MoonBoyDinoRider());
        addCreatureReady(player1, new FrenziedRaptor());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(moonBoy.getPowerModifier()).isZero();
        assertThat(moonBoy.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost resolves even if the last Dinosaur leaves in response")
    void boostsEvenIfDinosaurLeavesBeforeResolution() {
        Permanent moonBoy = addCreatureReady(player1, new MoonBoyDinoRider());
        Permanent dinosaur = addCreatureReady(player1, new FrenziedRaptor());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);
        gd.playerGraveyards.get(player1.getId()).add(dinosaur.getCard());
        resolveAllTriggers();

        assertThat(moonBoy.getPowerModifier()).isEqualTo(1);
        assertThat(moonBoy.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gaining a Dinosaur after attacking does not create an attack trigger")
    void gainingDinosaurAfterAttackDoesNotTrigger() {
        Permanent moonBoy = addCreatureReady(player1, new MoonBoyDinoRider());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new FrenziedRaptor());
        resolveAllTriggers();

        assertThat(moonBoy.getPowerModifier()).isZero();
        assertThat(moonBoy.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The reduction does not remove colored mana requirements")
    void reductionPreservesColoredManaRequirement() {
        harness.addToBattlefield(player1, new MoonBoyDinoRider());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Moon-Boy does not reduce Dinosaur spell costs")
    void opponentDoesNotReceiveCostReduction() {
        harness.addToBattlefield(player2, new MoonBoyDinoRider());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
