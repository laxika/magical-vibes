package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfTheEbonLegion());
        knight.setSummoningSick(false);
        return knight;
    }

    private void dealTwoDamage(Player target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
