package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RabidAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Any number of targets; grants +1/+0 and an ON_DEATH draw")
    void hasCorrectEffects() {
        RabidAttack card = new RabidAttack();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getMinTargets()).isZero();
        assertThat(card.getMaxTargets()).isEqualTo(99);

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        BoostTargetCreatureEffect boost =
                (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(new Fixed(1));
        assertThat(boost.toughnessBoost()).isEqualTo(new Fixed(0));

        GrantEffectToTargetUntilEndOfTurnEffect grant =
                (GrantEffectToTargetUntilEndOfTurnEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(grant.slot()).isEqualTo(EffectSlot.ON_DEATH);
        assertThat(grant.grantedEffect()).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Each targeted creature gets +1/+0 and a temporary death trigger")
    void boostsEachTargetedCreature() {
        Permanent bearA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bearB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, List.of(bearA.getId(), bearB.getId()));
        harness.passBothPriorities();

        for (Permanent bear : List.of(bearA, bearB)) {
            assertThat(bear.getPowerModifier()).isEqualTo(1);
            assertThat(bear.getToughnessModifier()).isZero();
            assertThat(bear.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).hasSize(1);
        }
    }

    @Test
    @DisplayName("A boosted creature dying draws a card for its controller")
    void deathDrawsCard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RabidAttack(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities(); // Rabid Attack resolves — bear gains the death trigger

        // Shock the 2/2 bear (toughness unchanged at 2) — it dies and the granted trigger fires
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities(); // Shock resolves → bear dies → death trigger onto stack
        harness.passBothPriorities(); // death trigger resolves → draw a card

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bear.getId()));
        // The lone library card was drawn by the granted "when this dies, draw a card" trigger
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        assertThat(bear.getPowerModifier()).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance through cleanup

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RabidAttack()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
