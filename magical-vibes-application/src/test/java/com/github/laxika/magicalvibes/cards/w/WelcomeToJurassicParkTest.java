package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.j.JurassicPark;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelcomeToJurassicPark.class, JurassicPark.class, MindStone.class, RaptorHatchling.class})
class WelcomeToJurassicParkTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I animates an opponent's noncreature artifact into a Wall")
    void chapterIAnimatesArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).contains(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Chapter II creates a hasty trampling Dinosaur")
    void chapterIICreatesDinosaur() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur).isNotNull();
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, dinosaur)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III destroys Walls before transforming into Jurassic Park")
    void chapterIIIDestroysWallsAndTransforms() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mind Stone");
        Permanent park = findPermanent(player1, "Jurassic Park");
        assertThat(park).isNotNull();
        assertThat(park.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Jurassic Park adds green mana for each Dinosaur you control")
    void jurassicParkAddsManaForDinosaurs() {
        Permanent park = harness.addToBattlefieldAndReturn(player1, new JurassicPark());
        harness.addToBattlefield(player1, new RaptorHatchling());
        harness.addToBattlefield(player1, new RaptorHatchling());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(park), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jurassic Park gives Dinosaur cards escape")
    void jurassicParkGivesDinosaursEscape() {
        harness.addToBattlefield(player1, new JurassicPark());
        List<RaptorHatchling> graveyard = List.of(
                new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling());
        harness.setGraveyard(player1, List.copyOf(graveyard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Hatchling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new WelcomeToJurassicPark());
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
