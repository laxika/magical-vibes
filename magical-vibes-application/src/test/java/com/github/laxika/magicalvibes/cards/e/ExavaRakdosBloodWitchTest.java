package com.github.laxika.magicalvibes.cards.e;

import java.util.List;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.r.RenegadeKrasis;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExavaRakdosBloodWitch.class, KraulWarrior.class, RenegadeKrasis.class})
class ExavaRakdosBloodWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castExava(true);

        Permanent exava = findPermanent(player1, "Exava, Rakdos Blood Witch");
        assertThat(exava.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, exava)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, exava)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castExava(false);

        assertThat(findPermanent(player1, "Exava, Rakdos Blood Witch")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Exava can't block")
    void unleashedCantBlock() {
        Permanent exava = addCreatureReady(player1, new ExavaRakdosBloodWitch());
        exava.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new KraulWarrior());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another creature you control with a +1/+1 counter gains haste")
    void counteredAllyGainsHaste() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ExavaRakdosBloodWitch());

        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        ally.setSummoningSick(true);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter does not gain haste")
    void uncounteredAllyStaysSummoningSick() {
        addCreatureReady(player1, new ExavaRakdosBloodWitch());

        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        ally.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature with a +1/+1 counter does not gain haste")
    void opponentCreatureUnaffected() {
        addCreatureReady(player1, new ExavaRakdosBloodWitch());

        Permanent ally = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        ally.setSummoningSick(true);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastCounterAllowsExavaToBlock() {
        Permanent exava = addCreatureReady(player1, new ExavaRakdosBloodWitch());
        exava.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        exava.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new KraulWarrior());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(exava);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void counterlessExavaCanAttackImmediately() {
        Permanent exava = harness.addToBattlefieldAndReturn(player1, new ExavaRakdosBloodWitch());
        exava.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void removingAllysLastCounterRemovesGrantedHaste() {
        addCreatureReady(player1, new ExavaRakdosBloodWitch());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        ally.setSummoningSick(true);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isTrue();

        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedHasteEndsWhenExavaLeaves() {
        Permanent exava = addCreatureReady(player1, new ExavaRakdosBloodWitch());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        ally.setSummoningSick(true);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(exava);
        gd.playerGraveyards.get(player1.getId()).add(exava.getCard());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void otherCounterTypesDoNotGrantHasteOrPreventBlocking() {
        Permanent exava = addCreatureReady(player1, new ExavaRakdosBloodWitch());
        exava.setCounterCount(CounterType.CHARGE, 1);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        ally.setSummoningSick(true);
        ally.setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player2, new KraulWarrior());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isFalse();
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(exava);
    }

    @Test
    void unleashCounterIsIncludedWhenCheckingEvolveOnEntry() {
        Permanent krasis = addCreatureReady(player1, new RenegadeKrasis());
        krasis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castExava(true);
        resolveAllTriggers();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Exava, Rakdos Blood Witch")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castExava(boolean unleash) {
        harness.setHand(player1, List.of(new ExavaRakdosBloodWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
