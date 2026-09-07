package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VraskaJoinsUp.class, GrizzlyBears.class, TymaretTheMurderKing.class, Mountain.class})
class VraskaJoinsUpTest extends BaseCardTest {

    @Test
    void entersWithDeathtouchCountersOnControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VraskaJoinsUp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.DEATHTOUCH)).isZero();
    }

    @Test
    void legendaryCreatureCombatDamageDrawsButNonlegendaryDamageDoesNot() {
        harness.addToBattlefield(player1, new VraskaJoinsUp());
        Permanent legendaryCreature = addCreatureReady(player1, new TymaretTheMurderKing());
        legendaryCreature.setAttacking(true);
        Permanent nonlegendaryCreature = addCreatureReady(player1, new GrizzlyBears());
        nonlegendaryCreature.setAttacking(true);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
