package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Smokestack.class, CoralMerfolk.class, Disenchant.class})
class SmokestackTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may add a soot counter during their upkeep")
    void controllerMayAddSootCounter() {
        Permanent smokestack = addSmokestack(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(smokestack.getCounterCount(CounterType.SOOT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the soot counter does not add one")
    void controllerMayDeclineSootCounter() {
        Permanent smokestack = addSmokestack(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(smokestack.getCounterCount(CounterType.SOOT)).isZero();
    }

    @Test
    @DisplayName("The controller also sacrifices permanents during their upkeep")
    void controllerSacrificesDuringOwnUpkeep() {
        Permanent smokestack = addSmokestack(player1);
        smokestack.setCounterCount(CounterType.SOOT, 1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        advanceToUpkeep(player1);
        choosePermanents(player1, permanent);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(battlefield(player1)).doesNotContain(permanent);
    }

    @Test
    @DisplayName("Each player sacrifices one permanent per soot counter during their upkeep")
    void eachPlayerSacrificesPerSootCounter() {
        Permanent smokestack = addSmokestack(player1);
        smokestack.setCounterCount(CounterType.SOOT, 2);
        Permanent player1Permanent = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent player1PermanentTwo = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent player2PermanentTwo = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent player2PermanentThree = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        advanceToUpkeep(player2);
        choosePermanents(player2, player2Permanent, player2PermanentTwo);

        assertThat(battlefield(player2)).doesNotContain(player2Permanent, player2PermanentTwo);
        assertThat(battlefield(player2)).contains(player2PermanentThree);
        assertThat(battlefield(player1)).contains(player1Permanent, player1PermanentTwo);
    }

    @Test
    @DisplayName("A player sacrifices all available permanents when they have fewer than the soot count")
    void sacrificesAllAvailablePermanents() {
        Permanent smokestack = addSmokestack(player1);
        smokestack.setCounterCount(CounterType.SOOT, 3);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(battlefield(player2)).doesNotContain(permanent);
    }

    @Test
    @DisplayName("With no soot counters, a player sacrifices no permanents")
    void noSootCountersCauseNoSacrifice() {
        addSmokestack(player1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(battlefield(player2)).contains(permanent);
    }

    @Test
    @DisplayName("Sacrifice trigger uses Smokestack's last known soot counters")
    void usesLastKnownSootCountersAfterSmokestackLeaves() {
        Permanent smokestack = addSmokestack(player1);
        smokestack.setCounterCount(CounterType.SOOT, 2);
        Permanent firstPermanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent secondPermanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player2, List.of(new Disenchant()));

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, smokestack.getId());
        harness.passBothPriorities();
        assertThat(battlefield(player1)).doesNotContain(smokestack);

        harness.passBothPriorities();

        assertThat(battlefield(player2)).doesNotContain(firstPermanent, secondPermanent);
    }

    private Permanent addSmokestack(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new Smokestack());
    }

    private void choosePermanents(Player player, Permanent... permanents) {
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player,
                java.util.Arrays.stream(permanents).map(Permanent::getId).toList());
    }

    private java.util.List<Permanent> battlefield(Player player) {
        return gd.playerBattlefields.get(player.getId());
    }
}
