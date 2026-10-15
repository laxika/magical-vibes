package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElendasHierophant.class, AngelOfMercy.class, Shock.class})
class ElendasHierophantTest extends BaseCardTest {

    @Test
    void putsCounterOnItWhenControllerGainsLife() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new ElendasHierophant());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hierophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void createsLifelinkVampiresEqualToItsPowerWhenItDies() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new ElendasHierophant());
        hierophant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, hierophant.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Vampire");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
    }

    @Test
    void separateLifeGainEventsEachPutOneCounterOnIt() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new ElendasHierophant());
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        for (int i = 0; i < 2; i++) {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(hierophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 26);
    }

    @Test
    void opponentGainingLifeDoesNotPutCounterOnIt() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new ElendasHierophant());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(hierophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void dyingBeforeLifeGainTriggerResolvesUsesPowerBeforePendingCounter() {
        Permanent hierophant = harness.addToBattlefieldAndReturn(player1, new ElendasHierophant());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(hierophant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 23);

        harness.castAndResolveInstant(player2, 0, hierophant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elenda's Hierophant");
        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
        assertThat(findPermanents(player2, "Vampire")).isEmpty();
    }
}
