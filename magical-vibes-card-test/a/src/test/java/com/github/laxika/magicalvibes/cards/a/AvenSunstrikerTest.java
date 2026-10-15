package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenSunstriker.class, BreakOpen.class})
class AvenSunstrikerTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new AvenSunstriker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent aven = findPermanent(player1, "Aven Sunstriker");
        assertThat(aven.isFaceDown()).isTrue();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aven));

        assertThat(aven.isFaceDown()).isFalse();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({AvenSunstriker.class, BreakOpen.class})
    void turningFaceUpWithSpellDoesNotGrantMegamorphCounter() {
        Permanent aven = harness.addToBattlefieldAndReturn(player2, new AvenSunstriker());
        aven.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new BreakOpen()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, aven.getId());

        assertThat(aven.isFaceDown()).isFalse();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingFaceUpDoesNotGrantMegamorphCounter() {
        harness.setHand(player1, List.of(new AvenSunstriker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent aven = findPermanent(player1, "Aven Sunstriker");
        assertThat(aven.isFaceDown()).isFalse();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void faceUpCreatureDealsBothCombatDamageSteps() {
        addCreatureReady(player1, new AvenSunstriker());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void faceDownCreatureDealsOnlyRegularCombatDamage() {
        Permanent aven = addCreatureReady(player1, new AvenSunstriker());
        aven.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void groundCreatureCannotBlockUntilAttackerIsFaceDown() {
        Permanent attacker = addCreatureReady(player1, new AvenSunstriker());
        Permanent blocker = addCreatureReady(player2, new AvenSunstriker());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
