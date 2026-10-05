package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FearOfIsolation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaitoBaneOfNightmares.class, FearOfIsolation.class})
class KaitoBaneOfNightmaresTest extends BaseCardTest {

    @Test
    void becomesCreatureWithoutPlaneswalkerTypeOnOwnTurn() {
        Permanent kaito = readyKaito();
        assertThat(gqs.isCreature(gd, kaito)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, kaito)).isFalse();
        assertThat(gqs.getEffectivePower(gd, kaito)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kaito)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, kaito, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void revertsOnOpponentsTurn() {
        Permanent kaito = readyKaito();
        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, kaito)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, kaito)).isTrue();
        assertThat(gqs.hasKeyword(gd, kaito, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void zeroLoyaltyDisablesAnimation() {
        Permanent kaito = readyKaito();
        kaito.setCounterCount(CounterType.LOYALTY, 0);
        assertThat(gqs.isCreature(gd, kaito)).isFalse();
        assertThat(gqs.hasKeyword(gd, kaito, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void emblemBoostsAnimatedKaitoButNotNonNinjas() {
        Permanent kaito = readyKaito();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FearOfIsolation());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, kaito)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kaito)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    void surveilsThenDrawsOnceForOpponentLifeLoss() {
        readyKaito();
        Card first = new FearOfIsolation();
        Card second = new FearOfIsolation();
        Card drawn = new FearOfIsolation();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, drawn));
        gd.lifeLostThisTurn.put(player2.getId(), 7);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void ownLifeLossDoesNotCauseDraw() {
        readyKaito();
        Card first = new FearOfIsolation();
        Card second = new FearOfIsolation();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void addsTwoStunCountersEvenToTappedCreature() {
        Permanent kaito = readyKaito();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearOfIsolation());
        target.tap();
        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void ninjutsuReturnsAttackerAndEntersTappedAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FearOfIsolation());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new KaitoBaneOfNightmares()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.assertInHand(player1, "Fear of Isolation");
        harness.assertNotOnBattlefield(player1, "Fear of Isolation");
        harness.passBothPriorities();
        Permanent kaito = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kaito.getCard()).isInstanceOf(KaitoBaneOfNightmares.class);
        assertThat(kaito.isTapped()).isTrue();
        assertThat(kaito.isAttacking()).isTrue();
        assertThat(kaito.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private Permanent readyKaito() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent kaito = harness.addToBattlefieldAndReturn(player1, new KaitoBaneOfNightmares());
        kaito.setCounterCount(CounterType.LOYALTY, 4);
        return kaito;
    }
}
