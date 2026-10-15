package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TriumphOfGerrard.class, GrizzlyBears.class, HillGiant.class})
class TriumphOfGerrardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers chapter I which awaits target selection for creature with greatest power")
    void etbTriggersChapterITargetSelection() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // Saga should be on battlefield with 1 lore counter
        Permanent saga = findPermanent(player1, "Triumph of Gerrard");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        // Chapter I requires targeting — should be awaiting input
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapter I only allows targeting the creature with greatest power")
    void chapterIOnlyTargetsGreatestPowerCreature() {
        // Hill Giant (3/3) and Grizzly Bears (2/2) — only Hill Giant should be targetable
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // The valid permanent choices should only include Hill Giant (greatest power)
        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();

        // Valid choices should contain Hill Giant but not Grizzly Bears
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(hillGiant.getId());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Chapter I puts +1/+1 counter on chosen creature with greatest power")
    void chapterIPutsCounterOnGreatestPowerCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities(); // resolve chapter I

        hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();
        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter I allows choice among tied greatest-power creatures")
    void chapterIAllowsTiedCreatures() {
        // Two Grizzly Bears (both 2/2) — both should be targetable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Both creatures should be valid choices (tied for greatest power)
        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.get(0).getId(), bears.get(1).getId());
    }

    @Test
    @DisplayName("Chapter I does not target opponent's creatures even if they have greater power")
    void chapterIDoesNotTargetOpponentCreatures() {
        // Player1 has Grizzly Bears (2/2), Player2 has Hill Giant (3/3)
        // Only player1's Grizzly Bears should be targetable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        Permanent opponentGiant = findPermanent(player2, "Hill Giant");
        assertThat(opponentGiant).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(opponentGiant.getId());

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        assertThat(ownBears).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(ownBears.getId());
    }

    @Test
    @DisplayName("Chapter I is removed from the stack when no legal target exists")
    void chapterINoCreaturesSkipsTargeting() {
        harness.setHand(player1, List.of(new TriumphOfGerrard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // A mandatory targeted trigger is removed when no legal target can be chosen.
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Chapter II puts a +1/+1 counter on creature with greatest power")
    void chapterIIPutsCounter() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = findPermanent(player1, "Triumph of Gerrard");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();

        // Only Hill Giant (3/3) should be targetable, not Grizzly Bears (2/2)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(hillGiant.getId());

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities(); // resolve chapter II

        hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();
        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III grants flying, first strike, and lifelink to greatest power creature")
    void chapterIIIGrantsKeywords() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = findPermanent(player1, "Triumph of Gerrard");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities(); // resolve chapter III

        hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant).isNotNull();
        assertThat(hillGiant.getGrantedKeywords()).contains(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = findPermanent(player1, "Triumph of Gerrard");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears).isNotNull();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve chapter III


        harness.assertNotOnBattlefield(player1, "Triumph of Gerrard");
        harness.assertInGraveyard(player1, "Triumph of Gerrard");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Every chapter requires a creature target and cannot be skipped")
    void chapterTargetIsMandatory(int chapter) {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Triumph of Gerrard");
        saga.setCounterCount(CounterType.LORE, chapter - 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(creature.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("A chapter fails if its target no longer has greatest power at resolution")
    void chapterRechecksGreatestPower(int chapter) {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Triumph of Gerrard");
        saga.setCounterCount(CounterType.LORE, chapter - 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hill Giant");
        harness.handlePermanentChosen(player1, giant.getId());
        findPermanent(player1, "Grizzly Bears").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(giant.getGrantedKeywords()).doesNotContain(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
        if (chapter == 3) {
            harness.assertNotOnBattlefield(player1, "Triumph of Gerrard");
            harness.assertInGraveyard(player1, "Triumph of Gerrard");
        }
    }

    @Test
    @DisplayName("Chapter III keywords survive Saga sacrifice and expire at end of turn")
    void chapterIIIKeywordsExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new TriumphOfGerrard());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Triumph of Gerrard").setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Triumph of Gerrard");
        assertThat(bears.getGrantedKeywords()).contains(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(bears.getGrantedKeywords()).doesNotContain(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }
}
