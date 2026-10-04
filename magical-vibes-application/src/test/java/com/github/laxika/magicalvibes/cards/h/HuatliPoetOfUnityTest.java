package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuatliPoetOfUnity.class, Forest.class, MineshaftSpider.class, ArmoredKincaller.class, MinimusContainment.class})
class HuatliPoetOfUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Huatli searches for a basic land when she enters")
    void entersAndSearchesForBasicLand() {
        Card forest = new Forest();
        harness.setHand(player1, List.of(new HuatliPoetOfUnity()));
        harness.setLibrary(player1, List.of(forest, new MineshaftSpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
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

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(huatli);
        assertThat(findPermanent(player1, "Roar of the Fifth People").isTransformed()).isTrue();
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

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"RED", "GREEN", "WHITE"})
    @DisplayName("Chapter II grants the mana ability to creatures entering later")
    void chapterIIGrantsManaAbilityToLaterCreatures(ManaColor color) {
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(new MineshaftSpider());
        harness.activateAbility(player1, indexOf(laterCreature), null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(laterCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chapter III searches for a Dinosaur card")
    void chapterIIISearchesForDinosaur() {
        Permanent saga = addSaga(2);
        Card dinosaur = new ArmoredKincaller();
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
        Permanent dinosaur = addCreatureReady(new ArmoredKincaller());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(4);
    }

    @Test
    void transformAcceptsMixedHybridPayment() {
        Permanent huatli = addFrontFace();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, indexOf(huatli), null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(huatli);
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(2);
        assertThat(findPermanent(player1, "Roar of the Fifth People").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void cannotTransformOutsideMainPhase() {
        Permanent huatli = addFrontFace();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(huatli), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(huatli);
    }

    @Test
    void transformReturnsToOwnerInsteadOfAbilityController() {
        HuatliPoetOfUnity card = new HuatliPoetOfUnity();
        card.setOwnerId(player2.getId());
        Permanent huatli = addCreatureReady(card);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, indexOf(huatli), null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Roar of the Fifth People");
        harness.assertOnBattlefield(player2, "Roar of the Fifth People");
        assertThat(countPermanents(player2, "Dinosaur")).isEqualTo(2);
    }

    @Test
    void chapterIIManaAbilityStopsWhenSagaLosesAbilities() {
        Permanent saga = addSaga(1);
        Permanent creature = addCreatureReady(new MineshaftSpider());
        advanceToNextChapter();
        resolveAllTriggers();

        prepareMainPhase();
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, saga.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(creature), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void chapterIIManaAbilityFollowsSagaController() {
        Permanent saga = addSaga(1);
        Permanent oldControllersCreature = addCreatureReady(new MineshaftSpider());
        Permanent newControllersCreature = addCreatureReady(player2, new MineshaftSpider());
        advanceToNextChapter();
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerBattlefields.get(player2.getId()).add(saga);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(oldControllersCreature), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(newControllersCreature), null, null);
        harness.handleListChoice(player2, "WHITE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void chapterIIManaAbilityEndsWhenSagaLeaves() {
        Permanent saga = addSaga(1);
        Permanent creature = addCreatureReady(new MineshaftSpider());
        advanceToNextChapter();
        resolveAllTriggers();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, saga);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(creature), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void chapterIIManaAbilityCannotBypassSummoningSickness() {
        addSaga(1);
        advanceToNextChapter();
        resolveAllTriggers();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        creature.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(creature), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void chapterIIICannotFindNonDinosaur() {
        addSaga(2);
        Card creature = new MineshaftSpider();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
    }

    @Test
    void chapterIVOnlyAffectsCurrentControlledDinosaursAndExpiresAtCleanup() {
        Permanent saga = addSaga(3);
        Permanent dinosaur = addCreatureReady(new ArmoredKincaller());
        Permanent otherCreature = addCreatureReady(new MineshaftSpider());
        Permanent opposingDinosaur = addCreatureReady(player2, new ArmoredKincaller());
        advanceToNextChapter();
        resolveAllTriggers();

        Permanent laterDinosaur = addCreatureReady(new ArmoredKincaller());
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        for (Permanent unaffected : List.of(otherCreature, opposingDinosaur, laterDinosaur)) {
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.DOUBLE_STRIKE)).isFalse();
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.TRAMPLE)).isFalse();
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void entersWithNoBasicLandAvailable() {
        Card creature = new MineshaftSpider();
        harness.setHand(player1, List.of(new HuatliPoetOfUnity()));
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertOnBattlefield(player1, "Huatli, Poet of Unity");
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
