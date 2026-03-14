package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MoldAdderTest extends BaseCardTest {

    @Test
    @DisplayName("Mold Adder has correct effect configuration")
    void hasCorrectEffects() {
        MoldAdder card = new MoldAdder();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped())
                .isInstanceOf(PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class);

        PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger =
                (PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect) mayEffect.wrapped();
        assertThat(trigger.triggerColors()).isEqualTo(Set.of(CardColor.BLUE, CardColor.BLACK));
        assertThat(trigger.amount()).isEqualTo(1);
        assertThat(trigger.onlyOwnSpells()).isFalse();
    }

    @Test
    @DisplayName("Opponent casting a blue spell triggers may ability and accepting adds counter")
    void opponentBlueSpellAcceptedAddsCounter() {
        harness.addToBattlefield(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        Permanent adder = getMoldAdder();
        assertThat(adder.getPlusOnePlusOneCounters()).isZero();

        harness.castCreature(player2, 0);

        // May ability should be pending
        assertThat(gd.pendingMayAbilities).hasSize(1);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability on the stack
        harness.passBothPriorities();

        assertThat(adder.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, adder)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, adder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a black spell triggers may ability and accepting adds counter")
    void opponentBlackSpellAcceptedAddsCounter() {
        harness.addToBattlefield(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new ChildOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Permanent adder = getMoldAdder();
        assertThat(adder.getPlusOnePlusOneCounters()).isZero();

        harness.castCreature(player2, 0);

        // May ability should be pending
        assertThat(gd.pendingMayAbilities).hasSize(1);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability on the stack
        harness.passBothPriorities();

        assertThat(adder.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining may ability does not add counter")
    void opponentBlueSpellDeclinedDoesNotAddCounter() {
        harness.addToBattlefield(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        Permanent adder = getMoldAdder();

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).hasSize(1);

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        assertThat(adder.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Opponent casting a green spell does not trigger Mold Adder")
    void opponentGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        Permanent adder = getMoldAdder();

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(adder.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Controller casting a blue spell does not trigger Mold Adder")
    void controllerBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoldAdder());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent adder = getMoldAdder();

        harness.castCreature(player1, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(adder.getPlusOnePlusOneCounters()).isZero();
    }

    private Permanent getMoldAdder() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mold Adder"))
                .findFirst()
                .orElseThrow();
    }
}
