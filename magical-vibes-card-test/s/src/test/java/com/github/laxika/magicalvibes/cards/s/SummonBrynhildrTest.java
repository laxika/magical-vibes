package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonBrynhildr.class, Shock.class, GrizzlyBears.class})
class SummonBrynhildrTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles the top card and grants play permission until end of turn")
    void chapterIExilesTopCardAndGrantsPlayPermission() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        addSaga(0);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Chapter II grants haste to only the next creature spell")
    void chapterIIGrantsHasteToNextCreatureSpellOnly() {
        addSaga(1);

        advanceToNextChapter();
        resolveAllTriggers();

        Permanent firstCreature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isTrue();

        Permanent secondCreature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Chapter III's haste trigger works after the Saga is sacrificed")
    void chapterIIITriggerSurvivesSagaLeavingTheBattlefield() {
        addSaga(2);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SummonBrynhildr);

        Permanent creature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void chapterICardCanBeCastOnTheChapterIITurn() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears()));
        addSaga(0);
        advanceToNextChapter();
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void chapterICardCanBeCastOnTheChapterIIITurnAfterSagaIsSacrificed() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addSaga(0);
        advanceToNextChapter();
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Summon: Brynhildr");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void noncreatureSpellDoesNotConsumeTheHasteTrigger() {
        addSaga(1);
        advanceToNextChapter();
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent creature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void grantedHasteExpiresAtEndOfTurn() {
        addSaga(1);
        advanceToNextChapter();
        resolveAllTriggers();
        Permanent creature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonBrynhildr());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent castCreatureAndResolve() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.castFromHand(player1, creature, "{1}{G}");
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }
}
