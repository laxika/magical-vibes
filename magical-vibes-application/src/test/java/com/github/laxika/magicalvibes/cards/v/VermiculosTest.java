package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VermiculosTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +4/+4 when an artifact enters")
    void artifactEnteringUnderYourControlBoosts() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets +4/+4 when an artifact enters under an opponent's control")
    void artifactEnteringUnderOpponentsControlBoosts() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact permanent enters")
    void nonArtifactDoesNotTrigger() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(1);
    }
}
