package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JewelEyedCobra;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SanctumWeaver;
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

@CardUsed({DressDown.class, SanctumWeaver.class, JewelEyedCobra.class, Opalescence.class})
class DressDownTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsCardOnEnter() {
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new JewelEyedCobra()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jewel-Eyed Cobra");
    }

    @Test
    @DisplayName("Creatures lose their abilities while it remains on the battlefield")
    void creaturesLoseAbilities() {
        Permanent weaver = addCreatureReady(player1, new SanctumWeaver());
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new JewelEyedCobra()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(weaver), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of each end step")
    void sacrificesAtEndStep() {
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new JewelEyedCobra()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dress Down");
        harness.assertInGraveyard(player1, "Dress Down");
    }

    @Test
    @DisplayName("Suppresses opposing creatures and restores their abilities after sacrifice")
    void opposingAbilitiesReturnAfterSacrifice() {
        Permanent weaver = addCreatureReady(player2, new SanctumWeaver());
        harness.addToBattlefield(player1, new DressDown());

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(weaver), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dress Down");
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(weaver), null, null);
        harness.handleListChoice(player2, ManaColor.GREEN.name());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can enter during an end step without triggering sacrifice that step")
    void flashDuringEndStepWaitsForNextEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new JewelEyedCobra()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        harness.assertInHand(player1, "Jewel-Eyed Cobra");
        harness.assertOnBattlefield(player1, "Dress Down");
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        harness.assertInGraveyard(player1, "Dress Down");
    }

    @Test
    @DisplayName("An animated Dress Down loses its own sacrifice ability")
    void animatedDressDownDoesNotSacrificeItself() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new DressDown());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dress Down");
    }

    @Test
    @DisplayName("An animated Dress Down has no enter-the-battlefield draw trigger")
    void animatedDressDownDoesNotDrawOnEntry() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new JewelEyedCobra()));
        addDressDownMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dress Down");
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Jewel-Eyed Cobra");
    }

    private void addDressDownMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
