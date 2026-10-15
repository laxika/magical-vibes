package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyriderPatrol.class, GreenwoodSentinel.class})
class SkyriderPatrolTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {G}{U} puts a counter on another creature and gives it flying")
    void payingManaBuffsAnotherCreature() {
        harness.addToBattlefield(player1, new SkyriderPatrol());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        UUID sourceId = harness.getPermanentId(player1, "Skyrider Patrol");
        UUID bearsId = harness.getPermanentId(player1, "Greenwood Sentinel");
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        UUID opponentBearsId = harness.getPermanentId(player2, "Greenwood Sentinel");

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(bearsId)
                .doesNotContain(sourceId, opponentBearsId);

        harness.handlePermanentChosen(player1, bearsId);
        Permanent beforeResolution = findPermanent(player1, "Greenwood Sentinel");
        assertThat(beforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Greenwood Sentinel");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the payment does not buff the target")
    void decliningPaymentDoesNothing() {
        harness.addToBattlefield(player1, new SkyriderPatrol());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent bears = findPermanent(player1, "Greenwood Sentinel");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SkyriderPatrol());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        UUID bearsId = harness.getPermanentId(player1, "Greenwood Sentinel");

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Greenwood Sentinel");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The payment trigger exists even without another creature to target")
    void triggersWithoutAnotherCreature() {
        harness.addToBattlefield(player1, new SkyriderPatrol());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new SkyriderPatrol());
        harness.addToBattlefield(player1, new GreenwoodSentinel());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        Permanent sentinel = findPermanent(player1, "Greenwood Sentinel");
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isFalse();
    }
}
