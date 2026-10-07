package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mirrormade;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.o.OKagachiMadeManifest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheKamiWar.class, OKagachiMadeManifest.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Shock.class, Mirrormade.class})
class TheKamiWarTest extends BaseCardTest {

    @Test
    void chapterIExilesOnlyAnOpponentsNonlandPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpposingPermanent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                opposingPermanent.getId(), secondOpposingPermanent.getId());

        harness.handlePermanentChosen(player1, opposingPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(ownPermanent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(opposingPermanent.getId()).contains(secondOpposingPermanent.getId(), opposingLand.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingPermanent.getCard());
    }

    @Test
    void chapterIIReturnsAnotherNonlandPermanentThenEachOpponentDiscards() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new Forest());
        Card discarded = new Shock();
        harness.setHand(player2, List.of(discarded));
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(ownPermanent.getId(), opposingPermanent.getId());

        harness.handlePermanentChosen(player1, opposingPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(opposingPermanent.getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(opposingPermanent.getCard().getId()).doesNotContain(discarded.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void chapterIIDiscardsEvenWhenNoOtherNonlandPermanentCanBeChosen() {
        Card discarded = new Shock();
        harness.setHand(player2, List.of(discarded));
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    void chapterIIIReturnsTheSagaTransformed() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof OKagachiMadeManifest)
                .findFirst()
                .orElse(null);
        assertThat(transformed).isNotNull();
        assertThat(transformed.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceLetsDefendingPlayerChooseANonlandCardAndBoostsItsPower() {
        TheKamiWar front = new TheKamiWar();
        Permanent kami = addCreatureReady(player1, front);
        kami.setCard(front.getBackFaceCard());
        kami.setTransformed(true);

        Card chosen = new HillGiant();
        Card otherNonland = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(chosen, otherNonland, land));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(chosen.getId(), otherNonland.getId());

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chosen);
        assertThat(kami.getPowerModifier()).isEqualTo(chosen.getManaValue());
    }

    @Test
    void chapterIICanDeclineAnAvailableTargetAndStillDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card discarded = new Shock();
        harness.setHand(player2, List.of(discarded));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    void chapterIICanReturnItsControllersPermanentWithoutMakingItsControllerDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card retained = new Shock();
        harness.setHand(player1, List.of(retained));
        harness.setHand(player2, List.of());
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(retained, target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void singleFacedCopyIsExiledByChapterIIIAndDoesNotReturn() {
        Permanent original = addSagaWithLore(0);
        Mirrormade copyCard = new Mirrormade();
        harness.castFromHand(player1, copyCard, "{1}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        resolveAllTriggers();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == copyCard)
                .findFirst().orElseThrow();
        copy.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(copyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(copyCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof OKagachiMadeManifest);
    }

    @Test
    void attackReturnsTheOnlyNonlandCardAndPowerBonusExpires() {
        TheKamiWar front = new TheKamiWar();
        Permanent kami = addCreatureReady(player1, front);
        kami.setCard(front.getBackFaceCard());
        kami.setTransformed(true);
        Card returned = new HillGiant();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(returned, land));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(kami.getPowerModifier()).isEqualTo(4);
        assertThat(kami.getToughnessModifier()).isZero();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(kami.getPowerModifier()).isZero();
    }

    @Test
    void attackWithOnlyLandsInGraveyardDoesNotReturnACardOrBoostPower() {
        TheKamiWar front = new TheKamiWar();
        Permanent kami = addCreatureReady(player1, front);
        kami.setCard(front.getBackFaceCard());
        kami.setTransformed(true);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(kami.getPowerModifier()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIDoesNotDiscardWhenItsOnlyChosenTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card retained = new HillGiant();
        harness.setHand(player2, List.of(new Shock(), retained));
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handlePermanentChosen(player1, target.getId()));
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIIReturnsANewUntappedCreatureWithoutLoreCounters() {
        Permanent saga = addSagaWithLore(2);
        saga.tap();
        Card originalCard = saga.getOriginalCard();

        advanceToNextChapter();

        Permanent kami = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == originalCard)
                .findFirst().orElseThrow();
        assertThat(kami.getId()).isNotEqualTo(saga.getId());
        assertThat(kami.getCard()).isInstanceOf(OKagachiMadeManifest.class);
        assertThat(kami.isTapped()).isFalse();
        assertThat(kami.isSummoningSick()).isTrue();
        assertThat(kami.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(originalCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(originalCard);
    }
    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheKamiWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
    }
}
