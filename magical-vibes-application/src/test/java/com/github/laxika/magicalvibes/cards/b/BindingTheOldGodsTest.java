package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.w.WoodlandChasm;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BindingTheOldGods.class, Forest.class, MaskedVandal.class, WoodlandChasm.class})
class BindingTheOldGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys a nonland permanent controlled by an opponent")
    void chapterIDestroysOpponentsNonlandPermanent() {
        Permanent target = addCreatureReady(player2, new MaskedVandal());
        addSaga(player1, 0);

        triggerChapter();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Chapter II puts a Forest from the library onto the battlefield tapped")
    void chapterIISearchesForTappedForest() {
        addSaga(player1, 1);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new MaskedVandal(), forest));

        triggerChapter();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        Permanent searchedForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == forest)
                .findFirst()
                .orElse(null);
        assertThat(searchedForest).isNotNull();
        assertThat(searchedForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(MaskedVandal.class);
    }

    @Test
    @DisplayName("Chapter III grants deathtouch to your creatures until end of turn")
    void chapterIIIGrantsTemporaryDeathtouch() {
        addSaga(player1, 2);
        Permanent ownCreature = addCreatureReady(player1, new MaskedVandal());
        Permanent opponentsCreature = addCreatureReady(player2, new MaskedVandal());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(opponentsCreature.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Chapter I cannot target a land or a permanent controlled by its controller")
    void chapterIOnlyOffersLegalOpponentNonlandTargets() {
        Permanent ownCreature = addCreatureReady(player1, new MaskedVandal());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent legalTarget = addCreatureReady(player2, new MaskedVandal());
        addSaga(player1, 0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(legalTarget.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(ownCreature.getId(), opponentLand.getId());
    }

    @Test
    void chapterIISearchesForANonbasicForest() {
        addSaga(player1, 1);
        WoodlandChasm forest = new WoodlandChasm();
        harness.setLibrary(player1, List.of(new MaskedVandal(), forest));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent searchedForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == forest).findFirst().orElseThrow();
        assertThat(searchedForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterIICanFailToFindEvenWhenAForestIsAvailable() {
        addSaga(player1, 1);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIDoesNothingWhenNoForestIsAvailable() {
        addSaga(player1, 1);
        MaskedVandal creature = new MaskedVandal();
        harness.setLibrary(player1, List.of(creature));

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIIDoesNotGrantDeathtouchToCreaturesEnteringLater() {
        Permanent saga = addSaga(player1, 2);
        Permanent creature = addCreatureReady(player1, new MaskedVandal());

        triggerChapter();
        harness.passBothPriorities();
        Permanent laterCreature = addCreatureReady(player1, new MaskedVandal());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "Binding the Old Gods");
    }

    @Test
    void chapterIIIIncludesTheSagaIfItIsACreature() {
        Permanent saga = addSaga(player1, 2);
        saga.setPermanentlyAnimated(true);
        saga.setPermanentAnimatedPower(4);
        saga.setPermanentAnimatedToughness(4);

        triggerChapter();
        saga.setCounterCount(CounterType.LORE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gqs.hasKeyword(gd, saga, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void enteringTheBattlefieldTriggersChapterI() {
        Permanent target = addCreatureReady(player2, new MaskedVandal());
        harness.setHand(player1, List.of(new BindingTheOldGods()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Binding the Old Gods").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Masked Vandal");
    }

    @Test
    void chapterIDoesNotDestroyATargetThatChangesToYourControl() {
        Permanent target = addCreatureReady(player2, new MaskedVandal());
        addSaga(player1, 0);

        triggerChapter();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    private Permanent addSaga(com.github.laxika.magicalvibes.model.Player player, int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player, new BindingTheOldGods());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
