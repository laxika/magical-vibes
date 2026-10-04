package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GideonChampionOfJustice.class, GrizzlyBears.class, GruulKeyrune.class, Shock.class})
class GideonChampionOfJusticeTest extends BaseCardTest {

    @Test
    void plusOneCountsTargetOpponentsCreaturesOnResolution() {
        Permanent gideon = addReadyGideon(player1, 4);
        addReadyCreature(player2);
        addReadyCreature(player2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        addReadyCreature(player2);
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    void zeroSnapshotsLoyaltyForPowerAndToughnessAndPreventsDamage() {
        Permanent gideon = addReadyGideon(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();

        gideon.setCounterCount(CounterType.LOYALTY, 7);
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, gideon.getId());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void minusFifteenExilesAllOtherPermanents() {
        Permanent gideon = addReadyGideon(player1, 16);
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opposingCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(gideon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
    }

    @Test
    void plusOneWithNoOpposingCreaturesOnlyAddsTheActivationCounter() {
        Permanent gideon = addReadyGideon(player1, 4);
        addReadyCreature(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneCannotTargetItsController() {
        addReadyGideon(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ultimateExilesNoncreatureArtifactsAsWellAsCreatures() {
        Permanent gideon = addReadyGideon(player1, 16);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new GruulKeyrune());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new GruulKeyrune());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(gideon);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(ownArtifact.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(opposingArtifact.getCard().getId())).isNotNull();
    }

    @Test
    void animationAndDamagePreventionExpireAtEndOfTurn() {
        Permanent gideon = addReadyGideon(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isPlaneswalker(gd, gideon)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, gideon)).isFalse();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, gideon.getId());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void ultimateStillResolvesAfterPayingTheLastFifteenLoyaltyCounters() {
        addReadyGideon(player1, 15);
        addReadyCreature(player1);
        addReadyCreature(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Gideon, Champion of Justice");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void ultimateExilesGideonIfHeLeavesAndReturnsBeforeResolution() {
        Permanent originalGideon = addReadyGideon(player1, 16);
        harness.activateAbility(player1, 0, 2, null, null);

        // Model a blink resolving above the ultimate: the same card returns as a new permanent.
        gd.playerBattlefields.get(player1.getId()).remove(originalGideon);
        Permanent returnedGideon = harness.addToBattlefieldAndReturn(player1, originalGideon.getCard());
        returnedGideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returnedGideon);
        assertThat(gd.findExiledCard(returnedGideon.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Gideon, Champion of Justice");
    }

    private Permanent addReadyGideon(Player player, int loyalty) {
        Permanent gideon = harness.addToBattlefieldAndReturn(player, new GideonChampionOfJustice());
        gideon.setCounterCount(CounterType.LOYALTY, loyalty);
        gideon.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gideon;
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
