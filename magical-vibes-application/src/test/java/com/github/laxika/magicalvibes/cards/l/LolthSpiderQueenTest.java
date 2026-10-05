package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({LolthSpiderQueen.class, GrizzlyBears.class, Shock.class})
class LolthSpiderQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a loyalty counter on Lolth when a creature you control dies")
    void putsLoyaltyCounterOnAllyCreatureDeath() {
        Permanent lolth = addReadyLolth(player1, 4);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(lolth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("0 ability draws a card and loses 1 life")
    void zeroAbilityDrawsAndLosesLife() {
        addReadyLolth(player1, 4);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("-3 creates two 2/1 Spider tokens with reach and menace")
    void minusThreeCreatesSpiderTokens() {
        Permanent lolth = addReadyLolth(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> spiders = findPermanents(player1, "Spider");
        assertThat(lolth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(spiders).hasSize(2);
        assertThat(spiders).allSatisfy(spider -> {
            assertThat(spider.getCard().getPower()).isEqualTo(2);
            assertThat(spider.getCard().getToughness()).isEqualTo(1);
            assertThat(spider.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
            assertThat(spider.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.REACH, Keyword.MENACE);
        });
    }

    @Test
    @DisplayName("-8 emblem makes an opponent lose life up to eight after combat damage")
    void emblemMakesOpponentLoseDifferenceAfterCombatDamage() {
        addReadyLolth(player1, 8);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstBear),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondBear)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("-8 emblem does not trigger when the opponent has already lost eight life")
    void emblemDoesNotTriggerAtEightLifeLost() {
        addReadyLolth(player1, 8);
        List<Permanent> attackers = List.of(
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, attackers.stream()
                .map(attacker -> gd.playerBattlefields.get(player1.getId()).indexOf(attacker))
                .toList());
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("An opponent's creature dying does not add loyalty")
    void opponentCreatureDeathDoesNotAddLoyalty() {
        Permanent lolth = addReadyLolth(player1, 4);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(lolth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("A Spider token dying adds loyalty")
    void tokenDeathAddsLoyalty() {
        Permanent lolth = addReadyLolth(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent spider = findPermanents(player1, "Spider").getFirst();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, spider.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spider")).hasSize(1);
        assertThat(lolth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The emblem includes noncombat life loss earlier in the turn")
    void emblemCountsEarlierNoncombatDamage() {
        addReadyLolth(player1, 8);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    private Permanent addReadyLolth(Player player, int loyalty) {
        Permanent permanent = addCreatureReady(player, new LolthSpiderQueen());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
