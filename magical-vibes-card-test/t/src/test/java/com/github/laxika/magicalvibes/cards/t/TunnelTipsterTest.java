package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GreenbeltRadical;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TunnelTipster.class, GreenbeltRadical.class, Murder.class})
class TunnelTipsterTest extends BaseCardTest {

    @Test
    void putsCounterOnEndStepAfterFaceDownCreatureEntered() {
        harness.setHand(player1, List.of(new TunnelTipster(), new GreenbeltRadical()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent tipster = findPermanent(player1, "Tunnel Tipster");
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterWithoutFaceDownCreatureEntry() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());

        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleFaceDownEntriesGiveOnlyOneCounter() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        castFaceDown(player1);
        castFaceDown(player1);

        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void faceDownEntryBeforeTipsterEnteredStillCounts() {
        castFaceDown(player1);
        harness.setHand(player1, List.of(new TunnelTipster()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent tipster = findPermanent(player1, "Tunnel Tipster");

        beginEndStep(player1);
        resolveAllTriggers();

        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsFaceDownEntryDoesNotCount() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        harness.forceActivePlayer(player2);
        castFaceDown(player2);

        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        castFaceDown(player1);

        beginEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void faceUpCreatureEntryDoesNotCount() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        harness.setHand(player1, List.of(new GreenbeltRadical()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void faceDownCreatureThatLeftBattlefieldStillCounts() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        Permanent radical = castFaceDown(player1);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, radical.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(radical);

        beginEndStep(player1);
        resolveAllTriggers();

        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureTurnedFaceUpBeforeEndStepStillCounts() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        Permanent radical = castFaceDown(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(radical));
        resolveAllTriggers();
        assertThat(radical.isFaceDown()).isFalse();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        beginEndStep(player1);
        resolveAllTriggers();

        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void previousTurnsFaceDownEntryDoesNotCountAgain() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        castFaceDown(player1);
        beginEndStep(player1);
        resolveAllTriggers();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        beginEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tappingAddsGreenManaImmediatelyToControllersPool() {
        Permanent tipster = addCreatureReady(player2, new TunnelTipster());

        harness.tapPermanent(player2, 0);

        assertThat(tipster.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alreadyTappedTipsterCannotProduceManaAgain() {
        Permanent tipster = addCreatureReady(player1, new TunnelTipster());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tipster.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void summoningSickTipsterCannotTapForMana() {
        harness.setHand(player1, List.of(new TunnelTipster()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent tipster = findPermanent(player1, "Tunnel Tipster");

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tipster.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void eachTipsterGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        castFaceDown(player1);

        beginEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removedSourceDoesNotReceiveCounterWhenTriggerResolves() {
        Permanent tipster = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        castFaceDown(player1);
        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, tipster.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tipster);
        assertThat(tipster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown(Player player) {
        harness.setHand(player, List.of(new GreenbeltRadical()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player, 0);
        resolveAllTriggers();
        Permanent creature = gd.playerBattlefields.get(player.getId()).getLast();
        assertThat(creature.isFaceDown()).isTrue();
        return creature;
    }

    private void beginEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
