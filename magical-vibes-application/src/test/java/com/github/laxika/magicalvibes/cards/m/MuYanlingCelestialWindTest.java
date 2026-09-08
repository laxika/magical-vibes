package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MuYanlingCelestialWindTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives a target creature -5/-0 until Mu Yanling's next turn")
    void plusOneDebuffsUntilNextTurn() {
        addReadyMuYanling(player1, 5);
        Permanent creature = addReadyCreature(player2, new AirElemental());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);

        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 can resolve without a target")
    void plusOneAllowsNoTarget() {
        Permanent muYanling = addReadyMuYanling(player1, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 returns up to two target creatures to their owners' hands")
    void minusThreeReturnsUpToTwoCreatures() {
        addReadyMuYanling(player1, 5);
        Permanent first = addReadyCreature(player2, new GrizzlyBears());
        Permanent second = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("-3 can return only one creature")
    void minusThreeReturnsOneCreature() {
        addReadyMuYanling(player1, 5);
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-3 cannot target a noncreature permanent")
    void minusThreeRejectsNonCreature() {
        addReadyMuYanling(player1, 5);
        Permanent artifact = addReadyPermanent(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 gives your flying creatures +5/+5 until end of turn")
    void minusSevenBoostsControlledFlyingCreatures() {
        addReadyMuYanling(player1, 7);
        Permanent flyer = addReadyCreature(player1, new AirElemental());
        Permanent groundCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingFlyer = addReadyCreature(player2, new AirElemental());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, groundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlyer)).isEqualTo(4);

        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    private Permanent addReadyMuYanling(Player player, int loyalty) {
        Permanent permanent = new Permanent(new MuYanlingCelestialWind());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
