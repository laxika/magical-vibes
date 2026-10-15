package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SkitteringSurveyor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisSentinel.class, TeferiTimebender.class, SkitteringSurveyor.class})
class TeferisSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +4/+0 (becomes 6/6) when controller controls a Teferi planeswalker")
    void boostWithTeferi() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, new TeferiTimebender());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("Base 2/6 without a Teferi planeswalker")
    void noBoostWithoutTeferi() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("No boost with a non-Teferi creature on the battlefield")
    void noBoostWithNonTeferiCreature() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, new SkitteringSurveyor());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("Loses +4/+0 when Teferi planeswalker leaves the battlefield")
    void losesBoostWhenTeferiLeaves() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, new TeferiTimebender());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);

        // Remove the Teferi planeswalker
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.TEFERI));

        // Boost should be gone immediately (computed on the fly)
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opponent's Teferi planeswalker does not grant the boost")
    void opponentTeferiDoesNotCount() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        harness.addToBattlefield(player2, new TeferiTimebender());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, new TeferiTimebender());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);

        // Simulate end-of-turn cleanup
        sentinel.resetModifiers();

        // Static boost should still be computed
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("Gains the boost immediately when Teferi enters after the Sentinel")
    void gainsBoostWhenTeferiEnters() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);

        harness.addToBattlefield(player1, new TeferiTimebender());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Teferi cards in hand and graveyard do not grant the boost")
    void teferiOutsideBattlefieldDoesNotCount() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        harness.setHand(player1, List.of(new TeferiTimebender()));
        harness.setGraveyard(player1, List.of(new TeferiTimebender()));

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("The bonus applies only to the Sentinel")
    void doesNotBoostOtherCreatures() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TeferisSentinel());
        Permanent surveyor = harness.addToBattlefieldAndReturn(player1, new SkitteringSurveyor());
        harness.addToBattlefield(player1, new TeferiTimebender());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, surveyor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, surveyor)).isEqualTo(2);
    }
}
