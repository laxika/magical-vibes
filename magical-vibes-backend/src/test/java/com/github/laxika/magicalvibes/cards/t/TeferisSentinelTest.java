package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeferisSentinelTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC ControlsSubtypeConditionalEffect(TEFERI) wrapping StaticBoostEffect(4, 0, SELF)")
    void hasCorrectStructure() {
        TeferisSentinel card = new TeferisSentinel();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);

        ControlsSubtypeConditionalEffect conditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.TEFERI);
        assertThat(conditional.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(4);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== With Teferi planeswalker =====

    @Test
    @DisplayName("Gets +4/+0 (becomes 6/6) when controller controls a Teferi planeswalker")
    void boostWithTeferi() {
        harness.addToBattlefield(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, createTeferiPlaneswalker());

        Permanent sentinel = findPermanent(player1, "Teferi's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    // ===== Without Teferi planeswalker =====

    @Test
    @DisplayName("Base 2/6 without a Teferi planeswalker")
    void noBoostWithoutTeferi() {
        harness.addToBattlefield(player1, new TeferisSentinel());

        Permanent sentinel = findPermanent(player1, "Teferi's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    @Test
    @DisplayName("No boost with a non-Teferi creature on the battlefield")
    void noBoostWithNonTeferiCreature() {
        harness.addToBattlefield(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent sentinel = findPermanent(player1, "Teferi's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    // ===== Loses boost when Teferi leaves =====

    @Test
    @DisplayName("Loses +4/+0 when Teferi planeswalker leaves the battlefield")
    void losesBoostWhenTeferiLeaves() {
        harness.addToBattlefield(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, createTeferiPlaneswalker());

        Permanent sentinel = findPermanent(player1, "Teferi's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);

        // Remove the Teferi planeswalker
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.TEFERI));

        // Boost should be gone immediately (computed on the fly)
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    // ===== Opponent's Teferi doesn't count =====

    @Test
    @DisplayName("Opponent's Teferi planeswalker does not grant the boost")
    void opponentTeferiDoesNotCount() {
        harness.addToBattlefield(player1, new TeferisSentinel());
        harness.addToBattlefield(player2, createTeferiPlaneswalker());

        Permanent sentinel = findPermanent(player1, "Teferi's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new TeferisSentinel());
        harness.addToBattlefield(player1, createTeferiPlaneswalker());

        Permanent sentinel = findPermanent(player1, "Teferi's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);

        // Simulate end-of-turn cleanup
        sentinel.resetModifiers();

        // Static boost should still be computed
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
    }

    // ===== Helper methods =====

    private Card createTeferiPlaneswalker() {
        // Create a card with the TEFERI subtype to simulate a Teferi planeswalker
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.TEFERI));
        return card;
    }

}
