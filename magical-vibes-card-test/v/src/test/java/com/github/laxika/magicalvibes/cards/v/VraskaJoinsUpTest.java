package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MiriamHerdWhisperer;
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

@CardUsed({VraskaJoinsUp.class, GrizzlyBears.class, TymaretTheMurderKing.class, Mountain.class,
        MiriamHerdWhisperer.class})
class VraskaJoinsUpTest extends BaseCardTest {

    @Test
    void entersWithDeathtouchCountersOnControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VraskaJoinsUp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

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

    @Test
    void entryCountersApplyToEveryCreaturePresentWhenTheTriggerResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new VraskaJoinsUp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(findPermanent(player1, "Vraska Joins Up").getCounterCount(CounterType.DEATHTOUCH))
                .isZero();

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(later.getCounterCount(CounterType.DEATHTOUCH)).isZero();
    }

    @Test
    void eachLegendaryCreatureDealingCombatDamageDrawsSeparately() {
        harness.addToBattlefield(player1, new VraskaJoinsUp());
        addCreatureReady(player1, new TymaretTheMurderKing()).setAttacking(true);
        addCreatureReady(player1, new MiriamHerdWhisperer()).setAttacking(true);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void opponentsLegendaryCreatureDoesNotDrawForEnchantmentController() {
        harness.addToBattlefield(player1, new VraskaJoinsUp());
        addCreatureReady(player2, new TymaretTheMurderKing()).setAttacking(true);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        int ownHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void legendaryCreatureNoncombatDamageDoesNotDraw() {
        harness.addToBattlefield(player1, new VraskaJoinsUp());
        harness.addToBattlefield(player1, new TymaretTheMurderKing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
