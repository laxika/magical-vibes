package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.e.EsperSentinel;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SolTalisman;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({UrzasSaga.class, Ornithopter.class, GrizzlyBears.class, ChromaticStar.class,
        EsperSentinel.class, SolTalisman.class})
class UrzasSagaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I grants Urza's Saga a colorless mana ability")
    void chapterIGrantsColorlessManaAbility() {
        Permanent saga = addSagaWithLore(0);
        resolveNextChapter();

        int sagaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(saga);
        harness.activateAbility(player1, sagaIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II creates an artifact-scaling Construct token")
    void chapterIICreatesConstructToken() {
        Permanent saga = addSagaWithLore(1);
        harness.addToBattlefield(player1, new Ornithopter());
        resolveNextChapter();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int sagaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(saga);
        harness.activateAbility(player1, sagaIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III puts a zero- or one-mana artifact from the library onto the battlefield")
    void chapterIIITutorsCheapArtifact() {
        Permanent saga = addSagaWithLore(2);
        Ornithopter ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of(ornithopter, new GrizzlyBears()));
        resolveNextChapter();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(ornithopter);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                p -> p.getCard() == ornithopter);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIIRequiresExactGenericManaCost() {
        addSagaWithLore(2);
        Ornithopter ornithopter = new Ornithopter();
        ChromaticStar star = new ChromaticStar();
        harness.setLibrary(player1, List.of(ornithopter, star, new EsperSentinel(), new SolTalisman()));
        resolveNextChapter();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(ornithopter, star);
        harness.handleCardChosen(player1, 1);
        assertThat(findPermanent(player1, "Chromatic Star").isTapped()).isFalse();
    }

    @Test
    void chapterIIIDoesNotRequireFindingAnArtifact() {
        Permanent saga = addSagaWithLore(2);
        Ornithopter ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of(ornithopter));
        resolveNextChapter();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ornithopter);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(countPermanents(player1, "Ornithopter")).isZero();
    }

    @Test
    void constructCountsOnlyItsControllersArtifactsAndUpdatesContinuously() {
        Permanent saga = addSagaWithLore(1);
        harness.addToBattlefield(player2, new Ornithopter());
        resolveNextChapter();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saga), 0, null, null);
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
    }

    @Test
    void playingSagaTriggersChapterIAndRetainsManaAbilityThroughChapterII() {
        harness.setHand(player1, List.of(new UrzasSaga()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        resolveAllTriggers();
        Permanent saga = findPermanent(player1, "Urza's Saga");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        int sagaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(saga);
        harness.activateAbility(player1, sagaIndex, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        saga.untap();
        resolveNextChapter();
        harness.activateAbility(player1, sagaIndex, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        saga.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, sagaIndex, 1, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Construct")).isEqualTo(1);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new UrzasSaga());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void resolveNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
