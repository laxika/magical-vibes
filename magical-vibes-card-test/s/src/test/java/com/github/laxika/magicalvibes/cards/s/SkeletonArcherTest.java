package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniAdversaryOfTyrants;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkeletonArcher.class, WalkingCorpse.class, AjaniAdversaryOfTyrants.class})
class SkeletonArcherTest extends BaseCardTest {

    private void castArcher(UUID targetId) {
        harness.setHand(player1, List.of(new SkeletonArcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB deals 1 damage to a target player")
    void etbDealsOneDamageToPlayer() {
        harness.setLife(player2, 20);

        castArcher(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skeleton Archer");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB deals 1 damage to a target creature")
    void etbDealsOneDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        castArcher(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void etbCanDamageItsController() {
        harness.setLife(player1, 20);

        castArcher(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbCanDamageFriendlyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        castArcher(target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    void etbRemovesOneLoyaltyFromPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AjaniAdversaryOfTyrants());
        target.setCounterCount(CounterType.LOYALTY, 4);

        castArcher(target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbStillDealsDamageAfterArcherLeavesBattlefield() {
        harness.setLife(player2, 20);
        castArcher(player2.getId());
        Permanent archer = findPermanent(player1, "Skeleton Archer");
        gd.playerBattlefields.get(player1.getId()).remove(archer);
        gd.playerGraveyards.get(player1.getId()).add(archer.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Skeleton Archer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbDoesNotDamageCreatureThatLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        castArcher(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Skeleton Archer");
        assertThat(gd.stack).isEmpty();
    }
}
