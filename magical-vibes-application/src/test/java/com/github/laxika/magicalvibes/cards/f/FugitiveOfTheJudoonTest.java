package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.m.MarthaJones;
import com.github.laxika.magicalvibes.cards.s.SarahJaneSmith;
import com.github.laxika.magicalvibes.cards.t.TheFourthDoctor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FugitiveOfTheJudoon.class, AccordersShield.class, MarthaJones.class,
        SarahJaneSmith.class, TheFourthDoctor.class})
class FugitiveOfTheJudoonTest extends BaseCardTest {

    @Test
    void chaptersOneAndTwoCreateThePrintedTokens() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent humanToken = findPermanents(player1, "Human").getFirst();
        Permanent rhinoToken = findPermanents(player1, "Alien Rhino").getFirst();
        assertThat(humanToken.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, humanToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, humanToken)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, humanToken, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, rhinoToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rhinoToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardTypes(gd, rhinoToken)).containsExactly(CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, rhinoToken, CardSubtype.ALIEN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, rhinoToken, CardSubtype.RHINO)).isTrue();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void chapterThreeExilesChosenHumanAndArtifactThenFindsADoctor() {
        Permanent saga = addSagaWithLore(2);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new SarahJaneSmith());
        Permanent secondHuman = harness.addToBattlefieldAndReturn(player1, new MarthaJones());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        TheFourthDoctor doctor = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(doctor));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).contains(human.getId(), secondHuman.getId());
        harness.handlePermanentChosen(player1, human.getId());

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validIds()).contains(artifact.getId(), secondArtifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(doctor);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(human.getCard().getId(), artifact.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(doctor.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(saga.getId()));
    }

    @Test
    void chapterThreeMayBeDeclined() {
        addSagaWithLore(2);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new SarahJaneSmith());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human, artifact);
        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FugitiveOfTheJudoon());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
