package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.Reveillark;
import com.github.laxika.magicalvibes.cards.u.Unearth;
import com.github.laxika.magicalvibes.cards.y.YixlidJailer;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullbriarTheWalkingGrave.class, Zombify.class, Unearth.class,
        Reveillark.class, YixlidJailer.class})
class SkullbriarTheWalkingGraveTest extends BaseCardTest {

    @Test
    void getsACounterWhenItDealsCombatDamageToAPlayer() {
        Permanent skullbriar = addCreatureReady(player1, new SkullbriarTheWalkingGrave());
        skullbriar.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(skullbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void keepsItsCountersWhenReturnedFromTheGraveyardToTheBattlefield() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, skullbriar));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Skullbriar, the Walking Grave");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void losesItsCountersWhenReturnedToItsOwnersHand() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, skullbriar));
        harness.castFromHand(player1, card, "{B}{G}");
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Skullbriar, the Walking Grave");

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsOnlyOneCounterForMultipleCombatDamage() {
        Permanent skullbriar = addCreatureReady(player1, new SkullbriarTheWalkingGrave());
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        skullbriar.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(skullbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void keepsNonPowerCountersThroughExile() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        skullbriar.setCounterCount(CounterType.CHARGE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, skullbriar));
        gd.removeFromExile(card.getId());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void losesCountersWhenItLeavesTheBattlefieldWithoutAbilities() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        skullbriar.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, skullbriar));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Skullbriar, the Walking Grave")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void losesCountersWhenItLeavesAGraveyardWithYixlidJailer() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, skullbriar));
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Skullbriar, the Walking Grave")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void retainedCountersMakeItTooLargeForReveillark() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, skullbriar));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, reveillark));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Skullbriar, the Walking Grave");
        harness.assertNotOnBattlefield(player1, "Skullbriar, the Walking Grave");
    }
}
