package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SongOfFreyalise.class, BalothGorger.class, LlanowarElves.class, InBolassClutches.class})
class SongOfFreyaliseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Song of Freyalise adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new SongOfFreyalise()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    @Test
    @DisplayName("Chapter I grants tap-for-mana ability to creatures you control")
    void chapterIGrantsManaAbilityToCreatures() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter I triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        // Baloth Gorger should have an "until next turn" activated ability
        Permanent bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears).isNotNull();
        assertThat(bears.getUntilNextTurnActivatedAbilities()).hasSize(1);
        assertThat(bears.getUntilNextTurnActivatedAbilities().getFirst().getDescription())
                .isEqualTo("{T}: Add one mana of any color.");
    }

    @Test
    @DisplayName("Mana ability granted by chapter I persists through end of turn")
    void manaAbilityPersistsThroughEndOfTurn() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        // Advance through cleanup using the engine.
        Permanent bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears).isNotNull();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Ability should still be present after end-of-turn cleanup.
        assertThat(bears.getUntilNextTurnActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Mana ability granted by chapter I is cleared at beginning of controller's next turn")
    void manaAbilityClearedAtNextTurn() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());
        resolveChapter(1);
        Permanent bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears.getUntilNextTurnActivatedAbilities()).hasSize(1);

        // Advance to player1's next turn — this should clear the abilities
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // end player2's turn → advance to player1's turn

        // Abilities should be cleared because it's now player1's turn
        bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears).isNotNull();
        assertThat(bears.getUntilNextTurnActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Chapter I does not grant ability to non-creature permanents")
    void chapterIDoesNotGrantAbilityToNonCreatures() {
        harness.addToBattlefield(player1, new SongOfFreyalise());

        // Add a second enchantment (non-creature)
        SongOfFreyalise secondEnchantment = new SongOfFreyalise();
        harness.addToBattlefield(player1, secondEnchantment);

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 0);

        // Both Sagas trigger chapter I; neither is a creature.
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise") && p != saga)
                .findFirst().ifPresent(p -> p.setCounterCount(CounterType.LORE, 0));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter triggers

        // Resolve all chapter abilities on stack
        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Enchantments should not have any granted abilities
        findPermanents(player1, "Song of Freyalise").forEach(p -> assertThat(p.getUntilNextTurnActivatedAbilities()).isEmpty());
    }

    @Test
    @DisplayName("Chapter III puts +1/+1 counters on all creatures you control")
    void chapterIIIPutsCountersOnCreatures() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        Permanent bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears).isNotNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent elves = findPermanent(player1, "Llanowar Elves");
        assertThat(elves).isNotNull();
        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III grants vigilance, trample, and indestructible until end of turn")
    void chapterIIIGrantsKeywords() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        Permanent bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears).isNotNull();
        assertThat(bears.getGrantedKeywords()).contains(
                Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE
        );
    }

    @Test
    @DisplayName("Chapter III keywords are cleared at end of turn but counters persist")
    void chapterIIIKeywordsClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());

        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Advance to end step, then cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to cleanup (resets "until end of turn" modifiers)

        Permanent bears = findPermanent(player1, "Baloth Gorger");
        assertThat(bears).isNotNull();

        // Keywords should be cleared
        assertThat(bears.getGrantedKeywords()).doesNotContain(
                Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE
        );

        // +1/+1 counters should persist
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        harness.assertNotOnBattlefield(player1, "Song of Freyalise");
        harness.assertInGraveyard(player1, "Song of Freyalise");
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        Permanent saga = findPermanent(player1, "Song of Freyalise");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }


    @Test
    void chapterIIGrantsOnlyToCreaturesPresentWhenItResolves() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player2, new BalothGorger());
        resolveChapter(2);
        harness.addToBattlefield(player1, new LlanowarElves());
        assertThat(findPermanent(player1, "Baloth Gorger").getUntilNextTurnActivatedAbilities()).hasSize(1);
        assertThat(findPermanent(player2, "Baloth Gorger").getUntilNextTurnActivatedAbilities()).isEmpty();
        assertThat(findPermanent(player1, "Llanowar Elves").getUntilNextTurnActivatedAbilities()).isEmpty();
    }

    @Test
    void chapterIIIExcludesOpponentsAndLaterCreatures() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player2, new BalothGorger());
        resolveChapter(3);
        harness.addToBattlefield(player1, new LlanowarElves());
        for (Permanent unaffected : List.of(findPermanent(player2, "Baloth Gorger"),
                findPermanent(player1, "Llanowar Elves"))) {
            assertThat(unaffected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(unaffected.getGrantedKeywords()).doesNotContain(
                    Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
        }
    }

    @Test
    void animatedSagaReceivesItsOwnChapterIManaAbility() {
        Permanent saga = addAnimatedSaga();
        resolveChapter(1);
        assertThat(saga.getUntilNextTurnActivatedAbilities()).hasSize(1);
    }

    @Test
    void stolenCreatureKeepsManaAbilityUntilOriginalGrantingPlayersNextTurn() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());
        resolveChapter(1);
        Permanent creature = findPermanent(player1, "Baloth Gorger");
        harness.setHand(player2, List.of(new InBolassClutches()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.getUntilNextTurnActivatedAbilities()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void grantedManaAbilityProducesChosenColorImmediately(ManaColor color) {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        Permanent creature = addCreatureReady(player1, new BalothGorger());
        resolveChapter(1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        harness.activateAbility(player1, index, null, null);
        assertThat(creature.isTapped()).isTrue();
        harness.handleListChoice(player1, color.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedManaAbilityCannotBeUsedWhileSummoningSick() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new BalothGorger());
        Permanent creature = findPermanent(player1, "Baloth Gorger");
        creature.setSummoningSick(true);
        resolveChapter(1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addAnimatedSaga() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        Permanent saga = findPermanent(player1, "Song of Freyalise");
        saga.setAnimatedUntilEndOfTurn(true);
        saga.setAnimatedPower(4);
        saga.setAnimatedToughness(4);
        return saga;
    }

    private void resolveChapter(int chapter) {
        findPermanent(player1, "Song of Freyalise").setCounterCount(CounterType.LORE, chapter - 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
