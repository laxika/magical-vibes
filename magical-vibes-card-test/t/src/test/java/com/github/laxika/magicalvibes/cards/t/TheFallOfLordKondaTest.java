package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BeholdTheUnspeakable;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FragmentOfKonda;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VisionOfTheUnspeakable;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFallOfLordKonda.class, FragmentOfKonda.class, DoomBlade.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, BeholdTheUnspeakable.class, VisionOfTheUnspeakable.class})
class TheFallOfLordKondaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles only an opponent's creature with mana value 4 or greater")
    void chapterIExilesEligibleOpponentCreature() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentGiant.getId())
                .doesNotContain(ownGiant.getId(), opponentBears.getId());

        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentGiant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentGiant.getCard());
    }

    @Test
    @DisplayName("Chapter II returns each permanent to its owner")
    void chapterIIReturnsPermanentsToOwners() {
        Permanent player1Permanent = addPermanentOwnedBy(player2, player1);
        Permanent player2Permanent = addPermanentOwnedBy(player1, player2);
        Permanent player2OwnPermanent = harness.addToBattlefieldAndReturn(player2, new Forest());

        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Permanent)
                .doesNotContain(player2Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Permanent, player2OwnPermanent)
                .doesNotContain(player1Permanent);
    }

    @Test
    @DisplayName("Chapter III transforms into Fragment of Konda, which draws when it dies")
    void chapterIIITransformsAndBackFaceDrawsOnDeath() {
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent fragment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FragmentOfKonda)
                .findFirst()
                .orElseThrow();
        assertThat(fragment.isTransformed()).isTrue();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, fragment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fragment);
    }

    @Test
    @DisplayName("Chapter I with no eligible creature does not stop later chapters")
    void chapterIWithNoLegalTargetStillAdvancesToChapterII() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent stolenLand = addPermanentOwnedBy(player2, player1);
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears, stolenLand);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolenLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(stolenLand);
    }

    @Test
    @DisplayName("Chapter I does not exile a target that leaves the battlefield in response")
    void chapterIDoesNotExileRemovedTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, giant.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giant.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(giant.getCard());
    }

    @Test
    @DisplayName("A stolen Saga returns transformed under the ability controller and draws for that player")
    void stolenSagaTransformsUnderControllerAndDrawsForController() {
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        TheFallOfLordKonda card = new TheFallOfLordKonda();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent fragment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FragmentOfKonda)
                .findFirst().orElseThrow();
        assertThat(fragment.getId()).isNotEqualTo(saga.getId());
        assertThat(fragment.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fragment);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, fragment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Chapter I can exile a transformed creature whose front face has mana value five")
    void chapterITargetsTransformedCreatureUsingFrontFaceManaValue() {
        harness.setHand(player2, List.of(new Forest()));
        Permanent opponentSaga = harness.addToBattlefieldAndReturn(player2, new BeholdTheUnspeakable());
        opponentSaga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vision = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VisionOfTheUnspeakable)
                .findFirst().orElseThrow();
        addSagaWithLore(0);
        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(vision.getId());
        harness.handlePermanentChosen(player1, vision.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(vision);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(vision.getOriginalCard());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFallOfLordKonda());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addPermanentOwnedBy(Player controller, Player owner) {
        Card card = new Forest();
        card.setOwnerId(owner.getId());
        return harness.addToBattlefieldAndReturn(controller, card);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
