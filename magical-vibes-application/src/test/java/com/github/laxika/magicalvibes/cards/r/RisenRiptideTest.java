package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShellShield;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisenRiptide.class, AcademyDrake.class, GrizzlyBears.class, ShellShield.class})
class RisenRiptideTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked spell makes Risen Riptide 5/5 until end of turn")
    void kickedSpellMakesItFiveFiveUntilEndOfTurn() {
        Permanent riptide = addReadyRiptide();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(riptide.getEffectivePower()).isEqualTo(5);
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting a non-kicked spell does not change Risen Riptide")
    void nonKickedSpellDoesNotTrigger() {
        Permanent riptide = addReadyRiptide();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("A spell with kicker does not trigger Risen Riptide when kicker is not paid")
    void unpaidKickerDoesNotTrigger() {
        Permanent riptide = addReadyRiptide();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, riptide.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("An opponent's kicked spell does not trigger your Risen Riptide")
    void opponentsKickedSpellDoesNotTrigger() {
        Permanent riptide = addReadyRiptide();
        Permanent opponentRiptide = addCreatureReady(player2, new RisenRiptide());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShellShield()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedInstant(player2, 0, opponentRiptide.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponentRiptide.getEffectivePower()).isEqualTo(5);
        assertThat(opponentRiptide.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The cast trigger resolves before the kicked spell and retains toughness modifiers")
    void triggerResolvesBeforeSpellAndModifiersApplyOnTop() {
        Permanent riptide = addReadyRiptide();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castKickedInstant(player1, 0, riptide.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(riptide.getEffectivePower()).isZero();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(riptide.getEffectivePower()).isEqualTo(5);
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
        harness.passBothPriorities();
        assertThat(riptide.getEffectivePower()).isEqualTo(5);
        assertThat(riptide.getEffectiveToughness()).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Repeated kicked spells keep the same base stats without removing existing bonuses")
    void repeatedKickedSpellsDoNotStackBaseStats() {
        Permanent riptide = addReadyRiptide();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShellShield(), new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castKickedInstant(player1, 0, riptide.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castKickedInstant(player1, 0, riptide.getId());
        harness.passBothPriorities();

        assertThat(riptide.getEffectivePower()).isEqualTo(5);
        assertThat(riptide.getEffectiveToughness()).isEqualTo(8);
        harness.passBothPriorities();
        assertThat(riptide.getEffectivePower()).isEqualTo(5);
        assertThat(riptide.getEffectiveToughness()).isEqualTo(11);
    }

    private Permanent addReadyRiptide() {
        return addCreatureReady(player1, new RisenRiptide());
    }
}
