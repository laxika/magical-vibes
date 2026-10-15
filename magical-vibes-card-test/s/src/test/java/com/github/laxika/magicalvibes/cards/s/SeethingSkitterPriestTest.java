package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeethingSkitterPriest.class, GrizzlyBears.class})
class SeethingSkitterPriestTest extends BaseCardTest {

    @Test
    void grantsTheDeathTriggerToControlledCreaturesAndCreatureCardsInHand() {
        Permanent battlefieldBear = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears handBear = new GrizzlyBears();
        harness.setHand(player1, List.of(handBear));
        Permanent priest = harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();

        kill(battlefieldBear);
        harness.passBothPriorities();

        harness.castFromHand(player1, handBear, "{1}{G}");
        harness.passBothPriorities();
        Permanent handBearPermanent = findPermanent(player1, "Grizzly Bears");

        kill(priest);
        harness.passBothPriorities();
        kill(handBearPermanent);
        harness.passBothPriorities();

        List<Permanent> mites = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MITE))
                .toList();
        assertThat(mites).hasSize(3);
        assertThat(mites).allSatisfy(mite -> assertThat(gqs.hasKeyword(gd, mite, Keyword.TOXIC))
                .isTrue());
    }

    @Test
    void repeatedGrantsCreateOneMiteForEachDeathAbility() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();

        kill(first);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mite")).isEqualTo(2);
    }

    @Test
    void repeatedGrantsToHandCardsRemainSeparateAfterCasting() {
        SeethingSkitterPriest handPriest = new SeethingSkitterPriest();
        harness.setHand(player1, List.of(handPriest));
        harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();

        harness.castFromHand(player1, handPriest, "{2}{W}{B}");
        resolveAllTriggers();
        Permanent castPriest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(handPriest.getId()))
                .findFirst().orElseThrow();
        kill(castPriest);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mite")).isEqualTo(3);
    }

    @Test
    void doesNotGrantToOpponentCreaturesOrCreaturesArrivingAfterResolution() {
        Permanent opposingPriest = harness.addToBattlefieldAndReturn(player2, new SeethingSkitterPriest());
        SeethingSkitterPriest opposingHandPriest = new SeethingSkitterPriest();
        harness.setHand(player2, List.of(opposingHandPriest));
        harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();
        Permanent latePriest = harness.addToBattlefieldAndReturn(player1, new SeethingSkitterPriest());

        kill(opposingPriest);
        kill(latePriest);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mite")).isZero();
        assertThat(countPermanents(player2, "Mite")).isZero();

        // Enter without resolving its own trigger to isolate the earlier grant.
        harness.setHand(player2, List.of());
        Permanent opposingHandPermanent = harness.enterBattlefieldAndReturn(player2, opposingHandPriest);
        kill(opposingHandPermanent);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countPermanents(player2, "Mite")).isZero();
    }

    @Test
    void grantUsesCreaturesPresentAtResolutionEvenIfThePriestHasDied() {
        Permanent priest = harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        kill(priest);
        Permanent arrivingPriest = harness.addToBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        SeethingSkitterPriest arrivingHandCard = new SeethingSkitterPriest();
        harness.setHand(player1, List.of(arrivingHandCard));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mite")).isZero();

        kill(arrivingPriest);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mite")).isEqualTo(1);

        harness.setHand(player1, List.of());
        Permanent handPermanent = harness.enterBattlefieldAndReturn(player1, arrivingHandCard);
        kill(handPermanent);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mite")).isEqualTo(2);
    }

    @Test
    void perpetualGrantSurvivesDeathAndReentry() {
        SeethingSkitterPriest card = new SeethingSkitterPriest();
        Permanent priest = harness.enterBattlefieldAndReturn(player1, card);
        resolveAllTriggers();
        kill(priest);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mite")).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).remove(card);
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        kill(returned);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mite")).isEqualTo(2);
    }

    @Test
    void existingMiteTokensAlsoReceiveTheDeathAbility() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();
        kill(first);
        resolveAllTriggers();
        Permanent mite = findPermanent(player1, "Mite");
        harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();

        kill(mite);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mite")).isEqualTo(1);
    }

    @Test
    void mitesCannotBlockAndGivePoisonAsCombatDamageIsDealt() {
        Permanent priest = harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();
        kill(priest);
        resolveAllTriggers();
        Permanent mite = findPermanent(player1, "Mite");
        assertThat(bls.canBlock(gd, mite)).isFalse();

        mite.setSummoningSick(false);
        mite.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(permanent.getEffectiveToughness());
        harness.runStateBasedActions();
    }
}
