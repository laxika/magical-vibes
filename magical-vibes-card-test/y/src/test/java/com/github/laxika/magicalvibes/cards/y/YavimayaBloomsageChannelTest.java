package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.Channel;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaBloomsageChannel.class, Channel.class, CrawWurm.class, GrizzlyBears.class})
class YavimayaBloomsageChannelTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on a target creature you control and prepares at power seven")
    void preparesWhenTargetReachesSevenPower() {
        Permanent bloomsage = addBloomsage();
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(bloomsage.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(bloomsage.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Adds the counter but does not prepare when the target remains below seven power")
    void doesNotPrepareBelowSevenPower() {
        Permanent bloomsage = addBloomsage();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bloomsage.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("The end-step trigger only offers creatures you control")
    void onlyTargetsOwnCreatures() {
        addBloomsage();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfAndPrepare() {
        Permanent bloomsage = addBloomsage();
        bloomsage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, bloomsage.getId());
        harness.passBothPriorities();

        assertThat(bloomsage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(bloomsage.isPrepared()).isTrue();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent bloomsage = addBloomsage();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bloomsage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bloomsage.isPrepared()).isFalse();
    }

    @Test
    void doesNotPrepareWhenTargetLeavesBeforeResolution() {
        Permanent bloomsage = addBloomsage();
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wurm));
        harness.passBothPriorities();

        assertThat(bloomsage.isPrepared()).isFalse();
        harness.assertInGraveyard(player1, "Craw Wurm");
    }

    @Test
    void repeatedPreparationKeepsTheSameSpellCopy() {
        Permanent bloomsage = addBloomsage();
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();
        var spellId = bloomsage.getPreparedSpellCardId();

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bloomsage.getPreparedSpellCardId()).isEqualTo(spellId);
    }

    @Test
    void castsPreparedChannelAndUnpreparesImmediately() {
        Permanent bloomsage = addBloomsage();
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();
        var spellId = bloomsage.getPreparedSpellCardId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, spellId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bloomsage.isPrepared()).isTrue();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spellId);

        assertThat(bloomsage.isPrepared()).isFalse();
        assertThat(bloomsage.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();
        harness.payLifeForColorlessMana(player1);

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Channel");
    }

    private Permanent addBloomsage() {
        return addCreatureReady(player1, new YavimayaBloomsageChannel());
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
