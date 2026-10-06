package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.cards.s.SecretRendezvous;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RowanScholarOfSparks.class, GrizzlyBears.class, Terminate.class, CampusGuide.class,
        HeatedDebate.class, SecretRendezvous.class, TimeWarp.class, DoublingSeason.class})
class RowanScholarOfSparksTest extends BaseCardTest {

    @Test
    void rowanDealsOneDamageWithoutThreeDraws() {
        addReadyRowan(5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void rowanDealsThreeDamageAfterThreeDraws() {
        addReadyRowan(5);
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void rowanEmblemMayPayToCopyInstant() {
        addReadyRowan(5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Terminate"));
    }

    @Test
    void willDrawsTwoCardsAndExilesPermanentsForElementalTokens() {
        RowanScholarOfSparks card = new RowanScholarOfSparks();
        Permanent will = harness.addToBattlefieldAndReturn(player1, card);
        will.setCard(card.getBackFaceCard());
        will.setTransformed(true);
        will.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        will.setCounterCount(CounterType.LOYALTY, 7);
        will.setLoyaltyActivationsThisTurn(0);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental")
                        && permanent.getEffectivePower() == 4
                        && permanent.getEffectiveToughness() == 4);
    }

    @Test
    void rowanDoesNotCountOpponentsDrawsOrTwoControllerDraws() {
        addReadyRowan(5);
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        gd.cardsDrawnThisTurn.put(player2.getId(), 10);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void rowanChecksDrawThresholdWhenAbilityResolves() {
        addReadyRowan(5);
        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        gd.cardsDrawnThisTurn.put(player1.getId(), 4);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void rowanCanBeCastUsingFrontFaceManaCost() {
        harness.setHand(player1, List.of(new RowanScholarOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void willCanBeCastUsingBackFaceManaCostAndDrawTwoCards() {
        harness.setHand(player1, List.of(new RowanScholarOfSparks()));
        harness.setLibrary(player1, List.of(new CampusGuide(), new CampusGuide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player1, 0, 1);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void rowanReducesInstantGenericCost() {
        addReadyRowan(5);
        assertInstantDiscount();
    }

    @Test
    void willReducesInstantGenericCost() {
        addReadyWill(5);
        assertInstantDiscount();
    }

    @Test
    void rowanReducesSorceryGenericCost() {
        addReadyRowan(5);
        assertSorceryDiscount();
    }

    @Test
    void willReducesSorceryGenericCost() {
        addReadyWill(5);
        assertSorceryDiscount();
    }

    @Test
    void discountDoesNotReduceColoredCosts() {
        addReadyRowan(5);
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void discountDoesNotApplyToCreatures() {
        addReadyRowan(5);
        harness.setHand(player1, List.of(new CampusGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void emblemCopiesInstantAndAllowsNewTarget() {
        addReadyRowan(5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent originalTarget = addCreatureReady(player2, new CampusGuide());
        Permanent copyTarget = addCreatureReady(player2, new CampusGuide());
        harness.setHand(player1, List.of(new HeatedDebate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Heated Debate")).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(originalTarget, copyTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void emblemMayDeclinePaymentForSorcery() {
        addReadyRowan(5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        prepareRendezvous(3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    void willSetsBaseStatsWithoutRemovingCountersAndExpiresOnNextTurn() {
        addReadyWill(5);
        Permanent target = addCreatureReady(player2, new CampusGuide());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void willBaseStatsExpireWhenControllerTakesAnExtraTurn() {
        addReadyWill(5);
        Permanent target = addCreatureReady(player2, new CampusGuide());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void willCanActivateEitherOptionalTargetAbilityWithNoTargets() {
        Permanent will = addReadyWill(7);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(will.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        will.setLoyaltyActivationsThisTurn(0);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(will.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(will);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void willExilesFivePermanentsAndGivesTokensToTheirControllers() {
        addReadyWill(8);
        Permanent ownTarget = addCreatureReady(player1, new CampusGuide());
        Permanent first = addCreatureReady(player2, new CampusGuide());
        Permanent second = addCreatureReady(player2, new CampusGuide());
        Permanent third = addCreatureReady(player2, new CampusGuide());
        Permanent fourth = addCreatureReady(player2, new CampusGuide());

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(ownTarget.getId(), first.getId(), second.getId(), third.getId(), fourth.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second, third, fourth);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elemental")).isEqualTo(4);
        assertThat(findPermanents(player2, "Elemental")).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        });
    }

    @Test
    void willExilesTokenReplacementBeforeCreatingAnyTokens() {
        addReadyWill(8);
        Permanent creature = addCreatureReady(player2, new CampusGuide());
        Permanent doublingSeason = harness.addToBattlefieldAndReturn(player2, new DoublingSeason());

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(creature.getId(), doublingSeason.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature, doublingSeason);
        assertThat(countPermanents(player2, "Elemental")).isEqualTo(2);
    }

    @Test
    void emblemCopiesSorceryOnceAfterRowanLeavesBattlefield() {
        addReadyRowan(4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rowan, Scholar of Sparks");
        prepareRendezvous(6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void assertInstantDiscount() {
        Permanent target = addCreatureReady(player2, new CampusGuide());
        harness.setHand(player1, List.of(new HeatedDebate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private void assertSorceryDiscount() {
        prepareRendezvous(3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    private void prepareRendezvous(int cards) {
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, cards)
                .mapToObj(i -> new CampusGuide()).toList());
        harness.setLibrary(player2, java.util.stream.IntStream.range(0, cards)
                .mapToObj(i -> new CampusGuide()).toList());
    }

    private Permanent addReadyWill(int loyalty) {
        Permanent will = addReadyRowan(loyalty);
        will.setCard(will.getCard().getBackFaceCard());
        will.setTransformed(true);
        return will;
    }
    private Permanent addReadyRowan(int loyalty) {
        Permanent rowan = harness.addToBattlefieldAndReturn(player1, new RowanScholarOfSparks());
        rowan.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return rowan;
    }
}
