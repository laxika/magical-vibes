package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SkullbriarTheWalkingGrave.class, Zombify.class})
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

        Permanent returned = findPermanentByCardId(card.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void losesItsCountersWhenReturnedToItsOwnersHand() {
        Card card = new SkullbriarTheWalkingGrave();
        Permanent skullbriar = addCreatureReady(player1, card);
        skullbriar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, skullbriar));
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
