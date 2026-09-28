package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDayOfTheDoctor.class, TheTenthDoctor.class, TheFourthDoctor.class,
        Forest.class, GrizzlyBears.class})
class TheDayOfTheDoctorTest extends BaseCardTest {

    @Test
    void chaptersExileToLegendaryAndLetTheFoundCardBePlayedNormally() {
        Permanent saga = addSagaWithLore(0);
        TheTenthDoctor found = new TheTenthDoctor();
        found.setSupertypes(Set.of(CardSupertype.LEGENDARY));
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
        Card found = new GrizzlyBears();
        found.setSupertypes(Set.of(CardSupertype.LEGENDARY));
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
        Permanent saga = addSagaWithLore(3);
        Permanent chosenDoctor = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Permanent otherDoctor = harness.addToBattlefieldAndReturn(player2, new TheFourthDoctor());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("The Day of the Doctor"));
    }

    @Test
    void chapterIVMayCanBeDeclined() {
        addSagaWithLore(3);
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheTenthDoctor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(doctor.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doctor, creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
