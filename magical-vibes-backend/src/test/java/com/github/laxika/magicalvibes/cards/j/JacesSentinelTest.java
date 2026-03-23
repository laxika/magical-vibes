package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JacesSentinelTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two STATIC ControlsSubtypeConditionalEffect(JACE) effects: StaticBoostEffect and GrantEffectEffect(CantBeBlockedEffect)")
    void hasCorrectStaticEffects() {
        JacesSentinel card = new JacesSentinel();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);

        // First effect: +1/+0 conditional on controlling a Jace
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);
        ControlsSubtypeConditionalEffect boostConditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(boostConditional.subtype()).isEqualTo(CardSubtype.JACE);
        assertThat(boostConditional.wrapped()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) boostConditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);

        // Second effect: can't be blocked conditional on controlling a Jace
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);
        ControlsSubtypeConditionalEffect unblockableConditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(unblockableConditional.subtype()).isEqualTo(CardSubtype.JACE);
        assertThat(unblockableConditional.wrapped()).isInstanceOf(GrantEffectEffect.class);
        GrantEffectEffect grantEffect = (GrantEffectEffect) unblockableConditional.wrapped();
        assertThat(grantEffect.effect()).isInstanceOf(CantBeBlockedEffect.class);
        assertThat(grantEffect.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional +1/+0 and can't be blocked with Jace =====

    @Test
    @DisplayName("Gets +1/+0 when controller controls a Jace planeswalker")
    void getsPowerBoostWithJace() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2); // 1 base + 1 bonus
    }

    @Test
    @DisplayName("Can't be blocked when controller controls a Jace planeswalker")
    void cantBeBlockedWithJace() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();
    }

    // ===== No bonus without a Jace =====

    @Test
    @DisplayName("No power boost without a Jace planeswalker")
    void noPowerBoostWithoutJace() {
        harness.addToBattlefield(player1, new JacesSentinel());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1); // 1 base, no bonus
    }

    @Test
    @DisplayName("Can be blocked without a Jace planeswalker")
    void canBeBlockedWithoutJace() {
        harness.addToBattlefield(player1, new JacesSentinel());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Non-Jace creature doesn't count =====

    @Test
    @DisplayName("Non-Jace creature does not grant bonus")
    void nonJaceDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Loses bonus when Jace leaves =====

    @Test
    @DisplayName("Loses +1/+0 and can't be blocked when Jace leaves the battlefield")
    void losesBonusWhenJaceLeaves() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();

        // Remove the Jace planeswalker
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.JACE));

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Opponent's Jace doesn't count =====

    @Test
    @DisplayName("Opponent's Jace planeswalker does not grant bonus")
    void opponentJaceDoesNotCount() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player2, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Helper methods =====

    private Card createJacePlaneswalker() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.JACE));
        return card;
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
