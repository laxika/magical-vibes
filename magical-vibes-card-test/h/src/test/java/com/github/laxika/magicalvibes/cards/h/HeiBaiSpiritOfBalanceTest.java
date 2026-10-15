package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TrustyBoomerang;
import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeiBaiSpiritOfBalance.class, TurtleDuck.class, TrustyBoomerang.class})
class HeiBaiSpiritOfBalanceTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to put two +1/+1 counters on Hei Bai")
    void etbSacrificesAnotherCreatureForCounters() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent heiBai = castHeiBai();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking may sacrifice another creature to put two +1/+1 counters on Hei Bai")
    void attackSacrificesAnotherCreatureForCounters() {
        Permanent heiBai = addCreatureReady(player1, new HeiBaiSpiritOfBalance());
        Permanent sacrifice = addCreatureReady(player1, new TurtleDuck());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The sacrifice can be an artifact")
    void etbSacrificesArtifactForCounters() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new TrustyBoomerang());
        Permanent heiBai = castHeiBai();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("When Hei Bai leaves, it puts every kind of counter it had on a creature")
    void leavingTransfersAllCounters() {
        Permanent recipient = addCreatureReady(player1, new TurtleDuck());
        Permanent heiBai = addCreatureReady(player1, new HeiBaiSpiritOfBalance());
        heiBai.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        heiBai.setCounterCount(CounterType.CHARGE, 3);

        removeHeiBai(heiBai);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void countersArePlacedDuringTheSacrificeAbilityResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent heiBai = castHeiBai();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackCountersArePlacedDuringTheSacrificeAbilityResolution() {
        Permanent heiBai = addCreatureReady(player1, new HeiBaiSpiritOfBalance());
        Permanent sacrifice = addCreatureReady(player1, new TurtleDuck());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent heiBai = castHeiBai();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeChoiceExcludesHeiBaiAndOpponentsPermanents() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        harness.addToBattlefield(player2, new TurtleDuck());
        harness.addToBattlefield(player2, new TrustyBoomerang());
        castHeiBai();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();
    }

    @Test
    void cannotSacrificeItselfWhenNoOtherPermanentIsAvailable() {
        Permanent heiBai = castHeiBai();

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(heiBai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heiBai);
    }

    @Test
    void leavingForHandTransfersCountersOnlyToACreatureYouControl() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        harness.addToBattlefield(player2, new TurtleDuck());
        harness.addToBattlefield(player1, new TrustyBoomerang());
        Permanent heiBai = harness.addToBattlefieldAndReturn(player1, new HeiBaiSpiritOfBalance());
        heiBai.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        heiBai.setCounterCount(CounterType.FLYING, 1);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, heiBai));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        resolveAllTriggers();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertInHand(player1, "Hei Bai, Spirit of Balance");
    }

    @Test
    void leavingWithoutCountersStillRequiresATarget() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent heiBai = harness.addToBattlefieldAndReturn(player1, new HeiBaiSpiritOfBalance());

        removeHeiBai(heiBai);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        resolveAllTriggers();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void leavingWithNoCreatureYouControlDoesNotOfferAnOpponentTarget() {
        harness.addToBattlefield(player2, new TurtleDuck());
        harness.addToBattlefield(player1, new TrustyBoomerang());
        Permanent heiBai = harness.addToBattlefieldAndReturn(player1, new HeiBaiSpiritOfBalance());
        heiBai.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        removeHeiBai(heiBai);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castHeiBai() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HeiBaiSpiritOfBalance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Hei Bai, Spirit of Balance");
    }

    private void removeHeiBai(Permanent heiBai) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, heiBai));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
