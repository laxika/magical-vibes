package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TuskguardCaptain.class, AlpineGrizzly.class})
class TuskguardCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Tuskguard Captain and taps it")
    void outlastPutsCounterAndTaps() {
        Permanent captain = addCaptainReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(captain.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addCaptainReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("A creature you control with a +1/+1 counter has trample")
    void counteredOwnCreatureHasTrample() {
        Permanent captain = addCaptainReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, captain, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain trample")
    void onlyCounteredOwnCreaturesHaveTrample() {
        addCaptainReady(player1);
        Permanent uncountered = addCreatureReady(player1, new AlpineGrizzly());
        Permanent opponentCreature = addCreatureReady(player2, new AlpineGrizzly());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is lost when the +1/+1 counter is removed")
    void trampleEndsWhenCounterIsRemoved() {
        addCaptainReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Outlast does not grant trample until its counter is placed on resolution")
    void outlastCounterWaitsForResolution() {
        Permanent captain = addCaptainReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, captain, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, captain, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Outlast cannot be activated while another ability is on the stack")
    void outlastRequiresEmptyStack() {
        addCaptainReady(player1);
        Permanent secondCaptain = addCaptainReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(secondCaptain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Outlast cannot be activated during an opponent's main phase")
    void outlastRequiresControllersTurn() {
        addCaptainReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A summoning-sick Captain cannot pay Outlast's tap cost")
    void outlastRequiresNoSummoningSickness() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new TuskguardCaptain());
        captain.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(captain.isTapped()).isFalse();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A tapped Captain cannot activate Outlast")
    void outlastRequiresUntappedCaptain() {
        Permanent captain = addCaptainReady(player1);
        captain.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters do not grant trample")
    void otherCounterTypesDoNotGrantTrample() {
        Permanent captain = addCaptainReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, captain, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Countered creatures lose granted trample when the Captain leaves")
    void trampleEndsWhenCaptainLeaves() {
        Permanent captain = addCaptainReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(captain);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addCaptainReady(Player player) {
        return addCreatureReady(player, new TuskguardCaptain());
    }
}
