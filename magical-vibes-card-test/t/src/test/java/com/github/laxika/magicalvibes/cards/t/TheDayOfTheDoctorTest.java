package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDayOfTheDoctor.class, TheTenthDoctor.class, TheFourthDoctor.class,
        Forest.class, CybermanPatrol.class})
class TheDayOfTheDoctorTest extends BaseCardTest {

    @Test
    void chaptersExileToLegendaryAndLetTheFoundCardBePlayedNormally() {
        Permanent saga = addSagaWithLore(0);
        TheTenthDoctor found = new TheTenthDoctor();
        Forest skipped = new Forest();
        harness.setLibrary(player1, List.of(skipped, found));

        advanceToNextChapter();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped);
        assertThat(gd.findExiledCard(found.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(found.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionSourcePermanents)
                .containsEntry(found.getId(), saga.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (com.github.laxika.magicalvibes.model.ManaColor color
                : com.github.laxika.magicalvibes.model.ManaColor.values()) {
            harness.addMana(player1, color, 10);
        }
        harness.castFromExile(player1, found.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Tenth Doctor");
        assertThat(gd.findExiledCard(found.getId())).isNull();
    }

    @Test
    void foundCardLosesPlayPermissionWhenTheSagaLeaves() {
        Permanent saga = addSagaWithLore(0);
        TheTenthDoctor found = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(new Forest(), found));

        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(found.getId());
        assertThat(gd.exilePlayPermissionSourcePermanents).doesNotContainKey(found.getId());
        assertThat(gd.findExiledCard(found.getId())).isNotNull();
    }

    @Test
    void chapterIVChoosesDoctorsThenMayExileAllOtherCreaturesAndDealDamage() {
        addSagaWithLore(3);
        Permanent chosenDoctor = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Permanent otherDoctor = harness.addToBattlefieldAndReturn(player2, new TheFourthDoctor());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CybermanPatrol());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CybermanPatrol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToNextChapter();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosenDoctor.getId(), otherDoctor.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenDoctor.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosenDoctor);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(otherDoctor, opposingCreature);
        assertThat(gd.findExiledCard(otherDoctor.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(ownCreature.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(opposingCreature.getCard().getId())).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 13);
        harness.assertNotOnBattlefield(player1, "The Day of the Doctor");
    }

    @Test
    void chapterIVMayCanBeDeclined() {
        addSagaWithLore(3);
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CybermanPatrol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(doctor.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doctor, creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void laterSearchChaptersExileTheFirstLegendaryCard(int loreCounters) {
        addSagaWithLore(loreCounters);
        Forest skipped = new Forest();
        TheTenthDoctor found = new TheTenthDoctor();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(skipped, found, remaining));

        advanceToNextChapter();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, skipped);
        assertThat(gd.findExiledCard(skipped.getId())).isNull();
        assertThat(gd.findExiledCard(found.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(found.getId(), player1.getId());
    }

    @Test
    void searchWithoutALegendaryCardReturnsEveryExiledCardToTheLibrary() {
        addSagaWithLore(0);
        Forest first = new Forest();
        CybermanPatrol second = new CybermanPatrol();
        harness.setLibrary(player1, List.of(first, second));

        advanceToNextChapter();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
    }

    @Test
    void searchWithAnEmptyLibraryDoesNothing() {
        addSagaWithLore(0);
        harness.setLibrary(player1, List.of());

        advanceToNextChapter();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void chapterControllerCanPlayTheCardAfterSagaChangesControlBeforeResolution() {
        Permanent saga = addSagaWithLore(0);
        TheTenthDoctor found = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(found));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(saga);
            gd.playerBattlefields.get(player2.getId()).add(saga);
        });

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(found.getId())).isNotNull();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 4);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castFromExile(player1, found.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "The Tenth Doctor");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saga);
    }

    @Test
    void chapterIVCanChooseNoDoctorsAndExileEveryCreature() {
        addSagaWithLore(3);
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CybermanPatrol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(doctor.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        harness.assertLife(player1, lifeBefore - 13);
    }

    @Test
    void chapterIVDealsDamageEvenWithoutCreaturesToExile() {
        addSagaWithLore(3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore - 13);
    }

    @Test
    void chapterIVSparesUpToThreeDoctorsFromEitherBattlefield() {
        addSagaWithLore(3);
        Permanent ownTenth = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Permanent ownFourth = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Permanent opposingTenth = harness.addToBattlefieldAndReturn(player2, new TheTenthDoctor());
        Permanent opposingFourth = harness.addToBattlefieldAndReturn(player2, new TheFourthDoctor());

        advanceToNextChapter();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                ownTenth.getId(), ownFourth.getId(), opposingTenth.getId(), opposingFourth.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(ownTenth.getId(), ownFourth.getId(), opposingTenth.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownTenth, ownFourth);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingTenth);
        assertThat(gd.findExiledCard(opposingFourth.getCard().getId())).isNotNull();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheDayOfTheDoctor());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
