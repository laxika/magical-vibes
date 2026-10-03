package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blink.class, CybermanPatrol.class, EnchantedEvening.class})
class BlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I shuffles a creature and its owner investigates")
    void chapterIShufflesCreatureAndOwnerInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CybermanPatrol());
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        addSagaWithLore(0);

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Chapter II creates an Alien Angel that stops being a creature when an opponent casts a creature spell")
    void chapterIITokenBecomesNoncreatureUntilEndOfTurn() {
        addSagaWithLore(1);
        resolveNextChapter();

        Permanent alienAngel = findPermanent(player1, "Alien Angel");
        assertThat(gqs.isCreature(gd, alienAngel)).isTrue();
        assertThat(gqs.getEffectivePower(gd, alienAngel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, alienAngel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, alienAngel, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, alienAngel, Keyword.VIGILANCE)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new CybermanPatrol(), "{2}");
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, alienAngel)).isFalse();
        assertThat(gqs.isArtifact(gd, alienAngel)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, alienAngel)).isTrue();
    }

    @Test
    @DisplayName("Chapter III also shuffles a target creature and investigates")
    void chapterIIIShufflesCreatureAndInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CybermanPatrol());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void tokenRetainsOtherCardTypesWhenItStopsBeingCreature() {
        addSagaWithLore(1);
        resolveNextChapter();
        Permanent token = findPermanent(player1, "Alien Angel");
        harness.addToBattlefield(player1, new EnchantedEvening());
        assertThat(gqs.isEnchantment(gd, token)).isTrue();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CybermanPatrol(), "{2}");
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isEnchantment(gd, token)).isTrue();
    }

    @Test
    void chapterIVCreatesTokenAndSacrificesSagaAfterResolution() {
        Permanent saga = addSagaWithLore(3);

        triggerNextChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien Angel")).hasSize(1);
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Alien Angel"))).isTrue();
        assertThat(gqs.isArtifact(gd, findPermanent(player1, "Alien Angel"))).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "Blink");
    }

    @Test
    void controllersCreatureSpellDoesNotTurnTokenIntoNoncreature() {
        addSagaWithLore(1);
        resolveNextChapter();
        Permanent token = findPermanent(player1, "Alien Angel");

        harness.castFromHand(player1, new CybermanPatrol(), "{2}");
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerToken() {
        addSagaWithLore(1);
        resolveNextChapter();
        Permanent token = findPermanent(player1, "Alien Angel");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Blink(), "{2}{U}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    void stolenCreaturesOwnerReceivesClueAndShuffledCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CybermanPatrol());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        int ownersLibrarySize = gd.playerDecks.get(player2.getId()).size();
        addSagaWithLore(0);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(ownersLibrarySize + 1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void illegalTargetDoesNotInvestigate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CybermanPatrol());
        addSagaWithLore(0);
        triggerNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Blink());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveNextChapter() {
        triggerNextChapter();
        harness.passBothPriorities();
    }
}
