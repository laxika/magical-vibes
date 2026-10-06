package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.cards.i.IllustriousHistorian;
import com.github.laxika.magicalvibes.cards.s.StartFromScratch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillardropWarden.class, HeatedDebate.class, IllustriousHistorian.class, StartFromScratch.class})
class PillardropWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns a target instant or sorcery to hand")
    void returnsTargetInstantOrSorceryFromGraveyard() {
        addReadyWarden();
        Card instant = new HeatedDebate();
        Card creature = new IllustriousHistorian();
        harness.setGraveyard(player1, List.of(instant, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Heated Debate");
        harness.assertInGraveyard(player1, "Illustrious Historian");
        harness.assertNotInGraveyard(player1, "Heated Debate");
        harness.assertNotOnBattlefield(player1, "Pillardrop Warden");
        harness.assertInGraveyard(player1, "Pillardrop Warden");
    }

    @Test
    @DisplayName("Cannot target a non-instant or non-sorcery card in the graveyard")
    void cannotTargetCreatureInGraveyard() {
        addReadyWarden();
        Card creature = new IllustriousHistorian();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addReadyWarden();
        Card instant = new HeatedDebate();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a sorcery during the postcombat main phase")
    void returnsSorceryDuringPostcombatMain() {
        addReadyWarden();
        Card sorcery = new StartFromScratch();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, sorcery.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Pillardrop Warden");
        harness.assertInGraveyard(player1, "Pillardrop Warden");
        harness.assertNotInHand(player1, "Start from Scratch");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Start from Scratch");
        harness.assertNotInGraveyard(player1, "Start from Scratch");
    }

    @Test
    @DisplayName("Cannot target an instant in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addReadyWarden();
        Card instant = new HeatedDebate();
        harness.setGraveyard(player2, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
        harness.assertInGraveyard(player2, "Heated Debate");
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void cannotActivateWithoutTarget() {
        addReadyWarden();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent warden = addReadyWarden();
        warden.tap();
        Card instant = new HeatedDebate();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent warden = addReadyWarden();
        warden.setSummoningSick(true);
        Card instant = new HeatedDebate();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
    }

    @Test
    @DisplayName("Cannot activate with less than two mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyWarden();
        Card instant = new HeatedDebate();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
        harness.assertInGraveyard(player1, "Heated Debate");
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringUpkeep() {
        addReadyWarden();
        Card instant = new HeatedDebate();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while another spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent warden = addReadyWarden();
        Card instant = new HeatedDebate();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new HeatedDebate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, warden.getId());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not return a replacement target when the target leaves the graveyard")
    void targetLeavingGraveyardDoesNotReturnAnotherCard() {
        Permanent warden = addReadyWarden();
        Card target = new HeatedDebate();
        Card other = new StartFromScratch();
        harness.setGraveyard(player1, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(other, warden.getCard()));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Heated Debate");
        harness.assertNotInHand(player1, "Start from Scratch");
        harness.assertInGraveyard(player1, "Start from Scratch");
        harness.assertInGraveyard(player1, "Pillardrop Warden");
        harness.assertNotOnBattlefield(player1, "Pillardrop Warden");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyWarden() {
        Permanent warden = addCreatureReady(player1, new PillardropWarden());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return warden;
    }
}
