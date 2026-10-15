package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarKyoshi;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLegendOfKyoshi.class, AvatarKyoshi.class, Forest.class, GrizzlyBears.class,
        HillGiant.class})
class TheLegendOfKyoshiTest extends BaseCardTest {

    @Test
    void chapterOneDrawsCardsEqualToGreatestControlledCreaturePower() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new HillGiant());
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void chapterTwoEarthbendsForCardsInHandAndAddsIsland() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    void chapterThreeReturnsTheSagaTransformed() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(avatar.getCard()).isInstanceOf(AvatarKyoshi.class);
    }

    @Test
    void avatarKyoshiGrantsKeywordsToControlledLandsAndAddsGreatestPowerMana() {
        Permanent avatar = addTransformedAvatar(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, forest, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void chapterOneDrawsNothingWithoutControlledCreatures() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player2, new HillGiant());
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringTheBattlefieldTriggersChapterOne() {
        harness.setHand(player1, List.of(new TheLegendOfKyoshi()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "The Legend of Kyoshi").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void chapterOneUsesGreatestPowerAtResolution() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(0);

        advanceToNextChapter();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void chapterTwoOnlyOffersControlledLandsAndCountsHandAtResolution() {
        harness.setHand(player1, List.of(new Forest()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addSaga(1);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
    }

    @Test
    void chapterTwoWithEmptyHandReturnsTheZeroToughnessLandTappedWithoutIsland() {
        harness.setHand(player1, List.of());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(forest.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.effectiveBasicLandTypes(gd, returned)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void chapterTwoDoesNotAffectTheLandAfterItLeavesAndReturns() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        Forest card = new Forest();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, card);
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, forest));
        gd.removeFromExile(card.getId());
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.effectiveBasicLandTypes(gd, returned)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    void chapterTwoIslandAndAnimationPersistAfterSagaTransforms() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Avatar Kyoshi").isTransformed()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void chapterThreeReturnsAnOpponentsSagaUnderTheAbilityControllersControl() {
        TheLegendOfKyoshi card = new TheLegendOfKyoshi();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);
        gd.stolenCreatures.put(saga.getId(), player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getOriginalCard().getId()).isEqualTo(card.getId());
                    assertThat(permanent.isTransformed()).isTrue();
                    assertThat(permanent.getCard()).isInstanceOf(AvatarKyoshi.class);
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(card.getId()));
    }

    @Test
    void chapterThreeCannotReturnTheSagaIfItAlreadyLeftTheBattlefield() {
        Permanent saga = addSaga(2);

        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, saga));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(saga.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void avatarKeywordsOnlyApplyToControlledLandsAndEndWhenAvatarLeaves() {
        Permanent avatar = addTransformedAvatar(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingLand, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingLand, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, avatar));

        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void avatarManaUsesEffectivePowerAndCanProduceAnotherColorWithoutUsingTheStack() {
        Permanent avatar = addTransformedAvatar(player1);
        avatar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opposingAvatar = addTransformedAvatar(player2);
        opposingAvatar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(avatar.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLegendOfKyoshi());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedAvatar(Player player) {
        TheLegendOfKyoshi front = new TheLegendOfKyoshi();
        Permanent avatar = addCreatureReady(player, front);
        avatar.setCard(front.getBackFaceCard());
        avatar.setTransformed(true);
        return avatar;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
