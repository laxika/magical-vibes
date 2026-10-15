package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfTheEbonLegion.class, Shock.class})
class KnightOfTheEbonLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives Knight +3/+3 and deathtouch until end of turn")
    void activatedAbilityBoostsAndGrantsDeathtouch() {
        Permanent knight = addReadyKnight();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
        assertThat(knight.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("End-step ability puts a counter on Knight when any player lost four life")
    void endStepCounterTriggersForAnyPlayer() {
        Permanent knight = addReadyKnight();
        dealTwoDamage(player2);
        dealTwoDamage(player2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("End-step ability also triggers when its controller lost four life")
    void endStepCounterTriggersForController() {
        Permanent knight = addReadyKnight();
        dealTwoDamage(player1);
        dealTwoDamage(player1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("End-step ability does not trigger below four life lost")
    void endStepCounterDoesNotTriggerBelowThreshold() {
        Permanent knight = addReadyKnight();
        dealTwoDamage(player2);

        advanceToEndStep(player1);

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life lost by different players is not combined for the threshold")
    void splitLifeLossDoesNotTrigger() {
        Permanent knight = addReadyKnight();
        dealTwoDamage(player1);
        dealTwoDamage(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Knight does not trigger during its opponent's end step")
    void opponentEndStepDoesNotTrigger() {
        Permanent knight = addReadyKnight();
        dealTwoDamage(player2);
        dealTwoDamage(player2);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Repeated activations stack and their effects expire at cleanup")
    void repeatedActivationsExpireAtCleanup() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfTheEbonLegion());
        int originalPower = gqs.getEffectivePower(gd, knight);
        int originalToughness = gqs.getEffectiveToughness(gd, knight);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(originalPower + 6);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(originalToughness + 6);
        assertThat(knight.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);

        advanceToEndStep(player1);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(originalToughness);
        assertThat(knight.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Losing more than four life still gives only one counter")
    void greaterLifeLossGivesOneCounter() {
        Permanent knight = addReadyKnight();
        dealTwoDamage(player2);
        dealTwoDamage(player2);
        dealTwoDamage(player2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfTheEbonLegion());
        knight.setSummoningSick(false);
        return knight;
    }

    private void dealTwoDamage(Player target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
