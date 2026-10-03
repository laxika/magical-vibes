package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
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

@CardUsed({CrookclawTransmuter.class, CoralTrickster.class, PrismaticLens.class})
class CrookclawTransmuterTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability switches a target creature's power and toughness")
    void switchesTargetCreaturePowerAndToughness() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CoralTrickster());
        trickster.setPowerModifier(1);
        harness.setHand(player1, List.of(new CrookclawTransmuter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trickster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trickster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Its enters-the-battlefield ability can target an opponent's creature")
    void switchesOpponentsCreaturePowerAndToughness() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CoralTrickster());
        harness.setHand(player1, List.of(new CrookclawTransmuter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trickster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trickster)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its power-and-toughness switch wears off at cleanup")
    void switchWearsOffAtCleanup() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CoralTrickster());
        trickster.setPowerModifier(1);
        harness.setHand(player1, List.of(new CrookclawTransmuter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trickster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trickster)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its enters-the-battlefield ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new CrookclawTransmuter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                harness.getPermanentId(player1, "Prismatic Lens")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enter an empty battlefield and target itself")
    void canTargetItself() {
        harness.setHand(player1, List.of(new CrookclawTransmuter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var transmuterId = harness.getPermanentId(player1, "Crookclaw Transmuter");
        harness.handlePermanentChosen(player1, transmuterId);
        harness.passBothPriorities();

        Permanent transmuter = gqs.findPermanentById(gd, transmuterId);
        assertThat(gqs.getEffectivePower(gd, transmuter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, transmuter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two enters-the-battlefield switches cancel each other")
    void twoSwitchesCancel() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CoralTrickster());
        harness.setHand(player1, List.of(new CrookclawTransmuter(), new CrookclawTransmuter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, trickster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trickster)).isEqualTo(2);

        harness.castCreature(player1, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trickster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trickster)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flash allows the creature and its switch during an opponent's turn")
    void canFlashInDuringOpponentsTurn() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CoralTrickster());
        harness.setHand(player1, List.of(new CrookclawTransmuter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, trickster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crookclaw Transmuter");
        assertThat(gqs.getEffectivePower(gd, trickster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trickster)).isEqualTo(2);
    }
}
