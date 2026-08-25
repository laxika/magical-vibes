package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuatliPoetOfUnity.class, Forest.class, GrizzlyBears.class, ColossalDreadmaw.class})
class HuatliPoetOfUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Huatli searches for a basic land when she enters")
    void entersAndSearchesForBasicLand() {
        Card forest = new Forest();
        harness.setHand(player1, List.of(new HuatliPoetOfUnity()));
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Huatli can transform into her Saga face at sorcery speed")
    void transformsIntoSagaFace() {
        Permanent huatli = addFrontFace();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, indexOf(huatli), null, null);
        harness.passBothPriorities();

        assertThat(huatli.isTransformed()).isTrue();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Chapter I creates two Dinosaur tokens")
    void chapterICreatesDinosaurTokens() {
        Permanent saga = addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II grants the mana ability to creatures entering later")
    void chapterIIGrantsManaAbilityToLaterCreatures() {
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(new GrizzlyBears());
        harness.activateAbility(player1, indexOf(laterCreature), null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(laterCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chapter III searches for a Dinosaur card")
    void chapterIIISearchesForDinosaur() {
        Permanent saga = addSaga(2);
        Card dinosaur = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(dinosaur, new Forest()));

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(dinosaur);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter IV gives Dinosaurs double strike and trample until end of turn")
    void chapterIVGrantsDoubleStrikeAndTrample() {
        Permanent saga = addSaga(3);
        Permanent dinosaur = addCreatureReady(new ColossalDreadmaw());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(4);
    }

    private Permanent addFrontFace() {
        return addCreatureReady(new HuatliPoetOfUnity());
    }

    private Permanent addSaga(int loreCounters) {
        HuatliPoetOfUnity card = new HuatliPoetOfUnity();
        Permanent saga = new Permanent(card);
        saga.setCard(card.getBackFaceCard());
        saga.setTransformed(true);
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private Permanent addCreatureReady(Card card) {
        return addCreatureReady(player1, card);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
