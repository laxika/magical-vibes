package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.cards.w.WaryThespian;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenScaleAeronaut.class, WaryThespian.class, VanquishTheWeak.class})
class GoldenScaleAeronautTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants flying")
    void backsUpAnotherCreature() {
        Permanent thespian = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        castGoldenScaleAeronaut();

        resolveEtbTargeting(thespian);

        assertThat(thespian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(thespian.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Backup's granted flying expires at the end of the turn")
    void grantedFlyingExpiresAtEndOfTurn() {
        Permanent thespian = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        castGoldenScaleAeronaut();
        resolveEtbTargeting(thespian);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thespian.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(thespian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can put its counter on the Aeronaut itself")
    void backsUpItself() {
        castGoldenScaleAeronaut();
        Permanent aeronaut = findPermanent(player1, "Golden-Scale Aeronaut");

        resolveEtbTargeting(aeronaut);

        assertThat(aeronaut.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can grant a counter and flying to an opponent's creature")
    void backsUpOpponentsCreature() {
        Permanent thespian = harness.addToBattlefieldAndReturn(player2, new WaryThespian());
        castGoldenScaleAeronaut();

        resolveEtbTargeting(thespian);

        assertThat(thespian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(thespian.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Backup still grants its counter and flying after its source is destroyed")
    void backupResolvesAfterSourceIsDestroyed() {
        Permanent thespian = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        castGoldenScaleAeronaut();
        Permanent aeronaut = findPermanent(player1, "Golden-Scale Aeronaut");
        harness.handlePermanentChosen(player1, thespian.getId());

        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, aeronaut.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aeronaut);
        resolveAllTriggers();

        assertThat(thespian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(thespian.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private void castGoldenScaleAeronaut() {
        harness.setHand(player1, List.of(new GoldenScaleAeronaut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
