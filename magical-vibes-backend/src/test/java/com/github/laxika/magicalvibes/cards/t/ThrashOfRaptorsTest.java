package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ThrashOfRaptorsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two STATIC effects: ControlsAnotherSubtypeConditionalEffect(DINOSAUR) wrapping StaticBoostEffect and GrantKeywordEffect")
    void hasCorrectStaticEffects() {
        ThrashOfRaptors card = new ThrashOfRaptors();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);

        // First effect: conditional +2/+0
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(ControlsAnotherSubtypeConditionalEffect.class);
        ControlsAnotherSubtypeConditionalEffect boostConditional =
                (ControlsAnotherSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(boostConditional.subtypes()).isEqualTo(Set.of(CardSubtype.DINOSAUR));
        assertThat(boostConditional.wrapped()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) boostConditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);

        // Second effect: conditional trample
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(ControlsAnotherSubtypeConditionalEffect.class);
        ControlsAnotherSubtypeConditionalEffect keywordConditional =
                (ControlsAnotherSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(keywordConditional.subtypes()).isEqualTo(Set.of(CardSubtype.DINOSAUR));
        assertThat(keywordConditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) keywordConditional.wrapped();
        assertThat(grant.keywords()).containsExactly(Keyword.TRAMPLE);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional boost =====

    @Test
    @DisplayName("Gets +2/+0 (becomes 5/3) when controller controls another Dinosaur")
    void boostWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    @Test
    @DisplayName("Base 3/3 without another Dinosaur")
    void noBoostWithoutAnotherDinosaur() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost with a non-Dinosaur creature")
    void noBoostWithNonDinosaur() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    // ===== Conditional trample =====

    @Test
    @DisplayName("Has trample when controller controls another Dinosaur")
    void hasTrampleWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not have trample when alone (no other Dinosaur)")
    void noTrampleWithoutAnotherDinosaur() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Loses boost/trample when Dinosaur leaves =====

    @Test
    @DisplayName("Loses +2/+0 and trample when the other Dinosaur leaves the battlefield")
    void losesBoostAndTrampleWhenDinosaurLeaves() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();

        // Remove the other Dinosaur
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Opponent's Dinosaur doesn't count =====

    @Test
    @DisplayName("Opponent's Dinosaur does not grant boost or trample")
    void opponentDinosaurDoesNotCount() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player2, createDinosaur());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Two Thrash of Raptors enable each other =====

    @Test
    @DisplayName("Two Thrash of Raptors grant each other +2/+0 and trample")
    void twoThrashOfRaptorsEnableEachOther() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new ThrashOfRaptors());

        List<Permanent> thrashes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thrash of Raptors"))
                .toList();

        assertThat(thrashes).hasSize(2);
        for (Permanent thrash : thrashes) {
            assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, thrash, Keyword.TRAMPLE)).isTrue();
        }
    }

    // ===== Static boost survives end-of-turn reset =====

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent thrash = findPermanent(player1, "Thrash of Raptors");
        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);

        thrash.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, thrash)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thrash)).isEqualTo(3);
    }

    // ===== Helper methods =====

    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
