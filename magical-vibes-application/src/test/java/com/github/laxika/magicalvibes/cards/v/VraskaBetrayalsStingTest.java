package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.a.ArmoredScrapgorger;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VraskaBetrayalsSting.class, CopperLonglegs.class, ArmoredScrapgorger.class})
class VraskaBetrayalsStingTest extends BaseCardTest {

    @Test
    @DisplayName("0 draws, loses life, and proliferates a loyalty counter")
    void zeroDrawsLosesLifeAndProliferates() {
        Permanent vraska = addReadyVraska(player1, 6);
        harness.setLibrary(player1, List.of(new CopperLonglegs()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(vraska.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("-2 turns a creature into a Treasure and grants its mana ability")
    void minusTwoTurnsCreatureIntoTreasure() {
        addReadyVraska(player1, 6);
        Permanent elves = addCreatureReady(player2, new ArmoredScrapgorger());

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, elves)).isTrue();
        assertThat(gqs.isCreature(gd, elves)).isFalse();
        assertThat(elves.getGrantedSubtypes()).contains(CardSubtype.TREASURE);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elves);
    }

    @Test
    @DisplayName("-2 cannot target a noncreature")
    void minusTwoCannotTargetNoncreature() {
        addReadyVraska(player1, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, addReadyVraska(player2, 6).getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-9 gives a target player enough poison counters to reach nine")
    void minusNineFillsPoisonCountersToNine() {
        addReadyVraska(player1, 9);
        gd.playerPoisonCounters.put(player2.getId(), 4);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("-9 does not add poison counters to a player already at nine")
    void minusNineDoesNothingAtNinePoisonCounters() {
        addReadyVraska(player1, 9);
        gd.playerPoisonCounters.put(player2.getId(), 9);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(9);
    }

    @Test
    void compleatedEntersWithSixLoyaltyWhenPaidWithMana() {
        harness.setHand(player1, List.of(new VraskaBetrayalsSting()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vraska, Betrayal's Sting")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 20);
    }

    @Test
    void compleatedEntersWithFourLoyaltyWhenPaidWithLife() {
        harness.setHand(player1, List.of(new VraskaBetrayalsSting()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vraska, Betrayal's Sting")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player1, 18);
    }

    @Test
    void treasureLosesCreatureSubtypes() {
        addReadyVraska(player1, 6);
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.TREASURE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.PHYREXIAN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.SPIDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void treasureConversionSurvivesVraskaLeavingAndTurnEnding() {
        Permanent vraska = addReadyVraska(player1, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArmoredScrapgorger());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vraska);
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, creature)).isTrue();
        assertThat(gqs.isCreature(gd, creature)).isFalse();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.BLUE.name());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void newlyEnteredCreatureCanUseTreasureManaAbilityAfterConversion() {
        addReadyVraska(player1, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void zeroCanProliferatePlayersAndEveryCounterKindOnAPermanent() {
        Permanent vraska = addReadyVraska(player1, 6);
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        creature.setCounterCount(CounterType.OIL, 2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setLibrary(player1, List.of(new CopperLonglegs()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void zeroAllowsChoosingNothingForProliferate() {
        Permanent vraska = addReadyVraska(player1, 6);
        harness.setLibrary(player1, List.of(new CopperLonglegs()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void minusNineCanTargetControllerAndUsesPoisonCountAtResolution() {
        addReadyVraska(player1, 9);
        gd.playerPoisonCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        gd.playerPoisonCounters.put(player1.getId(), 5);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(9);
    }

    private Permanent addReadyVraska(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VraskaBetrayalsSting());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
