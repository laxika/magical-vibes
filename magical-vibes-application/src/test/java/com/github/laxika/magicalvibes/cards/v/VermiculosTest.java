package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vermiculos.class, Frogmite.class, AncientDen.class, Forest.class})
class VermiculosTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +4/+4 when an artifact enters")
    void artifactEnteringUnderYourControlBoosts() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.castFromHand(player1, new Frogmite(), "{4}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets +4/+4 when an artifact enters under an opponent's control")
    void artifactEnteringUnderOpponentsControlBoosts() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Frogmite(), "{4}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact permanent enters")
    void nonArtifactDoesNotTrigger() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +4/+4 when an artifact land enters")
    void artifactLandEnteringBoosts() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.setHand(player1, List.of(new AncientDen()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets a separate +4/+4 boost for each artifact that enters")
    void eachArtifactEntryAddsAnotherBoost() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.enterBattlefieldAndReturn(player1, new Frogmite());
        harness.enterBattlefieldAndReturn(player1, new AncientDen());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(9);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent vermiculos = harness.addToBattlefieldAndReturn(player1, new Vermiculos());

        harness.castFromHand(player1, new Frogmite(), "{4}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vermiculos)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vermiculos)).isEqualTo(1);
    }
}
