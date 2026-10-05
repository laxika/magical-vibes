package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantOfTruth.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class MerchantOfTruthTest extends BaseCardTest {

    @Test
    void investigatesWhenANontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void cluesYouControlHaveExalted() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        assertThat(gqs.hasKeyword(gd, clue, Keyword.EXALTED)).isTrue();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    void investigatesWhenMerchantItselfDies() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Merchant of Truth");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesForItselfAndEachAllyDyingSimultaneously() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenAnOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenACreatureCopyTokenDies() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, token);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void eachClueBoostsALoneAttacker() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        createCluesByKillingBear();
        createCluesByKillingBear();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void multipleMerchantsGrantMultipleInstancesOfExaltedToEachClue() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        harness.addToBattlefield(player1, new MerchantOfTruth());
        createCluesByKillingBear();
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
    }

    @Test
    void cluesDoNotBoostCreaturesWhenTwoCreaturesAttack() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        createCluesByKillingBear();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void cluesLoseExaltedWhenMerchantLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        createCluesByKillingBear();
        Permanent clue = findPermanent(player1, "Clue");
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Merchant of Truth");
        assertThat(gqs.hasKeyword(gd, clue, Keyword.EXALTED)).isFalse();
    }

    private void createCluesByKillingBear() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
    }
}
