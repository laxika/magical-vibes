package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.f.FiresOfUndeath;
import com.github.laxika.magicalvibes.cards.g.Gravepurge;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalCatharUnhallowedCathar.class, FiresOfUndeath.class, YoungWolf.class,
        Gravepurge.class, FaithlessLooting.class})
class LoyalCatharUnhallowedCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Returns transformed at the beginning of the next end step")
    void returnsTransformedAtNextEndStep() {
        harness.addToBattlefield(player1, new LoyalCatharUnhallowedCathar());
        harness.setHand(player2, List.of(new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Loyal Cathar"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Loyal Cathar");
        harness.assertInGraveyard(player1, "Loyal Cathar");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Unhallowed Cathar");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Unhallowed Cathar");
        assertThat(returned).isNotNull();
        assertThat(returned.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Loyal Cathar");
    }

    @Test
    @DisplayName("Does not return if the card leaves the graveyard before the end step")
    void doesNotReturnIfNoLongerInGraveyard() {
        harness.addToBattlefield(player1, new LoyalCatharUnhallowedCathar());
        harness.setHand(player2, List.of(new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Loyal Cathar"));
        resolveAllTriggers();

        harness.setGraveyard(player1, List.of());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Unhallowed Cathar");
    }

    @Test
    @DisplayName("Unhallowed Cathar cannot block")
    void unhallowedCatharCannotBlock() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new LoyalCatharUnhallowedCathar());
        cathar.setCard(cathar.getOriginalCard().getBackFaceCard());
        cathar.setTransformed(true);
        harness.addToBattlefield(player2, new YoungWolf());

        assertThat(bls.canBlockAttacker(gd, cathar, findPermanent(player2, "Young Wolf"),
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    void delayedReturnDoesNotFollowCardThatLeavesAndReentersGraveyard() {
        LoyalCatharUnhallowedCathar cathar = new LoyalCatharUnhallowedCathar();
        harness.addToBattlefield(player1, cathar);
        harness.setHand(player2, List.of(new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Loyal Cathar"));
        resolveAllTriggers();

        harness.castFromHand(player1, new Gravepurge(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(cathar.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Loyal Cathar");
        harness.assertNotInGraveyard(player1, "Loyal Cathar");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new YoungWolf(), new YoungWolf()));
        gd.playerHands.get(player1.getId()).add(new FaithlessLooting());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Loyal Cathar");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Unhallowed Cathar");
        harness.assertInGraveyard(player1, "Loyal Cathar");
    }

    @Test
    void frontFaceAttacksWithoutTapping() {
        Permanent cathar = addCreatureReady(player1, new LoyalCatharUnhallowedCathar());
        harness.addToBattlefield(player2, new YoungWolf());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(cathar.isTapped()).isFalse();
        assertThat(cathar.isAttacking()).isTrue();
    }

    @Test
    void returnsUnderDyingCreaturesControllerRatherThanOwner() {
        LoyalCatharUnhallowedCathar card = new LoyalCatharUnhallowedCathar();
        card.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, card);
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Loyal Cathar"));
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Loyal Cathar");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Unhallowed Cathar");
        harness.assertNotOnBattlefield(player1, "Unhallowed Cathar");
    }

    @Test
    void backFaceDoesNotReturnAfterDying() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new LoyalCatharUnhallowedCathar());
        cathar.setCard(cathar.getOriginalCard().getBackFaceCard());
        cathar.setTransformed(true);
        harness.setHand(player2, List.of(new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, cathar.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Unhallowed Cathar");
        harness.assertInGraveyard(player1, "Loyal Cathar");
    }
}
