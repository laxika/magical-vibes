package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FaithsShield;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ChandraRoaringFlame.class, ChandraNalaar.class, GrizzlyBears.class, FaithsShield.class})
class ChandraRoaringFlameTest extends BaseCardTest {

    @Test
    @DisplayName("+1 deals 2 damage to target player and adds a loyalty counter")
    void plusOneDamagesPlayer() {
        Permanent chandra = addReadyChandra(player1, 4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("−2 deals 2 damage to target creature, killing a 2/2")
    void minusTwoKillsCreature() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("−7 deals 6 damage to each opponent and gives each damaged player an upkeep emblem")
    void ultimateDamagesAndEmblems() {
        Permanent chandra = addReadyChandra(player1, 7);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player2.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("The emblem deals 3 damage to its controller at the beginning of their upkeep")
    void emblemDealsDamageAtUpkeep() {
        addReadyChandra(player1, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int lifeAfterUltimate = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterUltimate - 3);
    }

    @Test
    @DisplayName("The emblem does not trigger on the opposing player's upkeep")
    void emblemDoesNotTriggerOnOtherPlayersUpkeep() {
        addReadyChandra(player1, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("+1 can damage a planeswalker instead of a player")
    void plusOneDamagesPlaneswalker() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A player whose ultimate damage is fully prevented gets no emblem")
    void fullyPreventedUltimateGivesNoEmblem() {
        addReadyChandra(player1, 7);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 5);
        harness.setHand(player2, List.of(new FaithsShield()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.handleListChoice(player2, "RED");
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 5);
        assertThat(gd.emblems).isEmpty();
    }

    @Test
    @DisplayName("Protection from red does not prevent the colorless emblem's upkeep damage")
    void emblemDamageIsColorless() {
        addReadyChandra(player1, 7);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setLife(player2, 5);
        harness.setHand(player2, List.of(new FaithsShield()));

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.handleListChoice(player2, "RED");
        resolveAllTriggers();

        harness.assertLife(player2, 2);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraRoaringFlame());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
