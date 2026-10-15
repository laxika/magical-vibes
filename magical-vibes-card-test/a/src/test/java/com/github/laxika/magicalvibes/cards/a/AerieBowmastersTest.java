package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.cards.t.TerritorialRoc;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerieBowmasters.class, SoulSummons.class, TerritorialRoc.class})
class AerieBowmastersTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new AerieBowmasters()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bowmasters = findPermanent(player1, "Aerie Bowmasters");
        assertThat(bowmasters.isFaceDown()).isTrue();
        assertThat(bowmasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bowmasters));

        assertThat(bowmasters.isFaceDown()).isFalse();
        assertThat(bowmasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotAddAMegamorphCounter() {
        harness.castFromHand(player1, new AerieBowmasters(), "{2}{G}{G}");
        harness.passBothPriorities();

        Permanent bowmasters = findPermanent(player1, "Aerie Bowmasters");
        assertThat(bowmasters.isFaceDown()).isFalse();
        assertThat(bowmasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manifestedBowmastersGetsNoCounterWhenTurnedFaceUpForItsManaCost() {
        harness.setLibrary(player1, List.of(new AerieBowmasters()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent bowmasters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bowmasters));

        assertThat(bowmasters.isFaceDown()).isFalse();
        assertThat(bowmasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manifestedBowmastersCanTurnFaceUpByPayingItsMegamorphCost() {
        harness.setLibrary(player1, List.of(new AerieBowmasters()));
        harness.castFromHand(player1, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        Permanent bowmasters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bowmasters));

        assertThat(bowmasters.isFaceDown()).isFalse();
        assertThat(bowmasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphCostDoesNotAddCounter() {
        harness.setHand(player1, List.of(new AerieBowmasters()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent bowmasters = findPermanent(player1, "Aerie Bowmasters");
        harness.inMutationScope(() -> gs.turnPermanentFaceUpWithoutPayingManaCost(gd, bowmasters));

        assertThat(bowmasters.isFaceDown()).isFalse();
        assertThat(bowmasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void reachAllowsBlockingFlyingOnlyWhileFaceUp() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new TerritorialRoc());
        harness.setHand(player1, List.of(new AerieBowmasters()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent bowmasters = findPermanent(player1, "Aerie Bowmasters");
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, bowmasters, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bowmasters));

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, bowmasters, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }
}
