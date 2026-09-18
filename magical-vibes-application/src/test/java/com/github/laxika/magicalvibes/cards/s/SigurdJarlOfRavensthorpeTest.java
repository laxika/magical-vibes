package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigurdJarlOfRavensthorpe.class, GrizzlyBears.class, HistoryOfBenalia.class})
class SigurdJarlOfRavensthorpeTest extends BaseCardTest {

    @Test
    @DisplayName("Boast puts a lore counter on a Saga and boosts another creature")
    void boastPutsLoreAndBoostsAnotherCreature() {
        Permanent saga = addSaga(0);
        Permanent sigurd = addCreatureReady(player1, new SigurdJarlOfRavensthorpe());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        sigurd.setAttackedThisTurn(true);

        activateBoast(sigurd, saga, 0);

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boast can remove a lore counter without triggering Sigurd's other-creature ability")
    void boastRemovesLoreWithoutBoosting() {
        Permanent saga = addSaga(1);
        Permanent sigurd = addCreatureReady(player1, new SigurdJarlOfRavensthorpe());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        sigurd.setAttackedThisTurn(true);

        activateBoast(sigurd, saga, 1);

        assertThat(saga.getCounterCount(CounterType.LORE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sigurd's triggered ability cannot target Sigurd himself")
    void triggeredAbilityRequiresAnotherCreature() {
        Permanent saga = addSaga(0);
        Permanent sigurd = addCreatureReady(player1, new SigurdJarlOfRavensthorpe());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        sigurd.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, 0, saga.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, sigurd.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Boast requires an attack and is limited to once each turn")
    void boastRestrictionsApply() {
        Permanent saga = addSaga(0);
        Permanent sigurd = addCreatureReady(player1, new SigurdJarlOfRavensthorpe());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int sigurdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sigurd);
        assertThatThrownBy(() -> harness.activateAbility(player1, sigurdIndex, 0, 0, saga.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");

        sigurd.setAttackedThisTurn(true);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        int firstSigurdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sigurd);
        harness.activateAbility(player1, firstSigurdIndex, 0, 0, saga.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        int finalSigurdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sigurd);
        assertThatThrownBy(() -> harness.activateAbility(player1, finalSigurdIndex, 0, 0, saga.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = new Permanent(new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void activateBoast(Permanent sigurd, Permanent saga, int modeIndex) {
        Permanent otherCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != sigurd
                        && permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int sigurdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sigurd);
        harness.activateAbility(player1, sigurdIndex, 0, modeIndex, saga.getId());
        harness.passBothPriorities();
        if (modeIndex == 0) {
            harness.handlePermanentChosen(player1, otherCreature.getId());
        }
        harness.passBothPriorities();
    }
}
