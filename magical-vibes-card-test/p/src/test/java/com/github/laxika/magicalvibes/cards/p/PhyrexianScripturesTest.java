package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ColdWaterSnapper;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianScriptures.class, PrimordialWurm.class, ColdWaterSnapper.class, Juggernaut.class})
class PhyrexianScripturesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers chapter I which awaits creature target selection")
    void etbTriggersChapterITargetSelection() {
        harness.addToBattlefield(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // Saga should be on battlefield with 1 lore counter
        Permanent saga = findPermanent(player1, "Phyrexian Scriptures");
        assertThat(saga).isNotNull();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        // Chapter I requires targeting — should be awaiting input
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapter I puts +1/+1 counter on chosen creature and makes it an artifact permanently")
    void chapterIResolvesCounterAndArtifactType() {
        harness.addToBattlefield(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers, awaits target

        GameData gd = harness.getGameData();

        Permanent creature = findPermanent(player1, "Primordial Wurm");
        assertThat(creature).isNotNull();

        // Choose Primordial Wurm as target
        harness.handlePermanentChosen(player1, creature.getId());

        // Chapter I ability should now be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // Creature should have +1/+1 counter
        creature = findPermanent(player1, "Primordial Wurm");
        assertThat(creature).isNotNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Creature should be permanently an artifact
        assertThat(creature.getPersistentGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(creature)).isTrue();
    }

    @Test
    @DisplayName("Chapter I artifact type persists across turn resets")
    void chapterIArtifactTypeSurvivesTurnReset() {
        harness.addToBattlefield(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        Permanent creature = findPermanent(player1, "Primordial Wurm");
        assertThat(creature).isNotNull();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities(); // resolve chapter I

        // Simulate turn reset (resetModifiers clears transient but not persistent)
        creature.resetModifiers();

        // Persistent card type should survive
        assertThat(creature.getPersistentGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(creature)).isTrue();
    }

    @Test
    @DisplayName("Chapter I with no creatures on battlefield pushes ability with no target")
    void chapterINoCreaturesSkipsTargeting() {
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();

        // No creatures → chapter I should be on the stack with no target (no awaiting input)
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));
    }

    @Test
    @DisplayName("Chapter I skip option — choosing self as target skips effect")
    void chapterISkipByChoosingPlayer() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Choose self (skip)
        harness.handlePermanentChosen(player1, player1.getId());

        // Chapter I ability on stack with no target
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I")
                && e.getTargetId() == null);

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // Opponent's creature should NOT be an artifact
        Permanent creature = findPermanent(player2, "Primordial Wurm");
        assertThat(creature).isNotNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getPersistentGrantedCardTypes()).doesNotContain(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Chapter II destroys all nonartifact creatures")
    void chapterIIDestroysNonartifactCreatures() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());
        harness.addToBattlefield(player1, new PrimordialWurm());
        harness.addToBattlefield(player2, new PrimordialWurm());

        Permanent saga = findPermanent(player1, "Phyrexian Scriptures");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        harness.passBothPriorities(); // resolve chapter II

        // Both nonartifact creatures should be destroyed
        long p1Creatures = countPermanents(player1, "Primordial Wurm");
        long p2Creatures = countPermanents(player2, "Primordial Wurm");
        assertThat(p1Creatures).isZero();
        assertThat(p2Creatures).isZero();
    }

    @Test
    @DisplayName("Chapter II does not destroy artifact creatures")
    void chapterIIDoesNotDestroyArtifactCreatures() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());
        harness.addToBattlefield(player1, new PrimordialWurm());

        Permanent saga = findPermanent(player1, "Phyrexian Scriptures");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 1);

        // Make the Primordial Wurm an artifact permanently (simulating chapter I effect)
        Permanent creature = findPermanent(player1, "Primordial Wurm");
        assertThat(creature).isNotNull();
        creature.getPersistentGrantedCardTypes().add(CardType.ARTIFACT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        // Creature (now an artifact) should survive
        boolean creatureSurvive = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Primordial Wurm"));
        assertThat(creatureSurvive).isTrue();
    }

    @Test
    @DisplayName("Chapter III exiles all opponents' graveyards but not controller's")
    void chapterIIIExilesOpponentGraveyardsOnly() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());

        // Put some cards in both graveyards
        harness.setGraveyard(player1, List.of(new PrimordialWurm()));
        harness.setGraveyard(player2, List.of(new PrimordialWurm(), new PrimordialWurm()));

        Permanent saga = findPermanent(player1, "Phyrexian Scriptures");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Opponent's graveyard should be exiled
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // Opponent's cards should be in exile
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);

        // Controller's graveyard should contain original card + sacrificed Saga
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Primordial Wurm")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new PhyrexianScriptures());

        Permanent saga = findPermanent(player1, "Phyrexian Scriptures");
        assertThat(saga).isNotNull();
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        harness.assertNotOnBattlefield(player1, "Phyrexian Scriptures");
        harness.assertInGraveyard(player1, "Phyrexian Scriptures");
    }

    @Test
    @DisplayName("Chapter I cannot target an opposing hexproof creature")
    void chapterIRejectsOpposingHexproofCreature() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player2, new ColdWaterSnapper());
        castScriptures();
        if (gd.interaction.isAwaitingInput()) {
            assertThat(gd.interaction.activeInteraction(
                    PendingInteraction.PermanentChoice.class)
                    .validIds()).doesNotContain(snapper.getId());
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();
        assertThat(snapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isArtifact(snapper)).isFalse();
    }

    @Test
    @DisplayName("Chapter I can target an opponent's creature")
    void chapterICanTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        castScriptures();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isArtifact(creature)).isTrue();
        assertThat(gqs.isCreature(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Chapter II preserves printed artifact creatures and destroys hexproof creatures")
    void chapterIIPreservesArtifactsButDestroysHexproofCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new PhyrexianScriptures());
        harness.addToBattlefield(player2, new Juggernaut());
        harness.addToBattlefield(player2, new ColdWaterSnapper());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Juggernaut");
        harness.assertNotOnBattlefield(player2, "Cold-Water Snapper");
        harness.assertInGraveyard(player2, "Cold-Water Snapper");
    }

    private void castScriptures() {
        harness.setHand(player1, List.of(new PhyrexianScriptures()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }
}
