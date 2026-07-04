package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThornfistStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Has Ward {1} and an infusion anthem granting +1/+0 and trample")
    void hasCorrectStructure() {
        ThornfistStriker card = new ThornfistStriker();

        var ward = card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL);
        assertThat(ward).hasSize(1);
        assertThat(((CounterUnlessPaysEffect) ward.getFirst()).amount()).isEqualTo(1);

        var statics = card.getEffects(EffectSlot.STATIC);
        assertThat(statics).hasSize(1);
        ConditionalEffect conditional = (ConditionalEffect) statics.getFirst();
        assertThat(conditional.condition()).isInstanceOf(GainedLifeThisTurn.class);
        StaticBoostEffect boost = (StaticBoostEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.grantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("No anthem when you have not gained life this turn")
    void noAnthemWithoutLifeGain() {
        harness.addToBattlefield(player1, new ThornfistStriker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Other creatures you control get +1/+0 and trample while you have gained life")
    void anthemWhileLifeGained() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        // The Striker itself is included ("creatures you control").
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ward triggers a counter-unless-pay when an opponent's spell targets it")
    void wardTriggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new ThornfistStriker());
        UUID strikerId = harness.getPermanentId(player1, "Thornfist Striker");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, strikerId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Thornfist Striker");
    }
}
