package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SongOfFreyaliseTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Chapter I grants activated mana ability to own creatures until next turn")
    void chapterIHasCorrectEffect() {
        SongOfFreyalise card = new SongOfFreyalise();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(GrantActivatedAbilityEffect.class);
    }

    @Test
    @DisplayName("Chapter II has same effect as chapter I")
    void chapterIIHasCorrectEffect() {
        SongOfFreyalise card = new SongOfFreyalise();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_II);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(GrantActivatedAbilityEffect.class);
    }

    @Test
    @DisplayName("Chapter III puts +1/+1 counters and grants keywords")
    void chapterIIIHasCorrectEffects() {
        SongOfFreyalise card = new SongOfFreyalise();

        var effects = card.getEffects(EffectSlot.SAGA_CHAPTER_III);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(PutPlusOnePlusOneCounterOnEachOwnCreatureEffect.class);
        assertThat(effects.get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== ETB: first lore counter and chapter I triggers =====

    @Test
    @DisplayName("Casting Song of Freyalise adds a lore counter and triggers chapter I")
    void castingAddsLoreCounterAndTriggersChapterI() {
        harness.setHand(player1, List.of(new SongOfFreyalise()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment

        GameData gd = harness.getGameData();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        assertThat(saga.getLoreCounters()).isEqualTo(1);

        // Chapter I ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getDescription()).contains("chapter I");
    }

    // ===== Chapter I: grants mana ability to creatures until next turn =====

    @Test
    @DisplayName("Chapter I grants tap-for-mana ability to creatures you control")
    void chapterIGrantsManaAbilityToCreatures() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter I triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getLoreCounters()).isEqualTo(1);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // Grizzly Bears should have an "until next turn" activated ability
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getUntilNextTurnActivatedAbilities()).hasSize(1);
        assertThat(bears.getUntilNextTurnActivatedAbilities().getFirst().getDescription())
                .isEqualTo("{T}: Add one mana of any color.");
    }

    @Test
    @DisplayName("Mana ability granted by chapter I persists through end of turn")
    void manaAbilityPersistsThroughEndOfTurn() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter I triggers
        harness.passBothPriorities(); // resolve chapter I

        // Simulate end-of-turn cleanup by calling resetModifiers directly
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        bears.resetModifiers();

        // Ability should still be present after end-of-turn cleanup (resetModifiers does not clear it)
        assertThat(bears.getUntilNextTurnActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Mana ability granted by chapter I is cleared at beginning of controller's next turn")
    void manaAbilityClearedAtNextTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        // Simulate the ability already being granted
        bears.getUntilNextTurnActivatedAbilities().add(
                new ActivatedAbility(
                        true, null,
                        List.of(new AwardAnyColorManaEffect()),
                        "{T}: Add one mana of any color."
                )
        );
        assertThat(bears.getUntilNextTurnActivatedAbilities()).hasSize(1);

        // Advance to player1's next turn — this should clear the abilities
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // end player2's turn → advance to player1's turn

        GameData gd = harness.getGameData();

        // Abilities should be cleared because it's now player1's turn
        bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
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

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(0);

        // Set second enchantment lore counters to 0 so only first one triggers
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise") && p != saga)
                .findFirst().ifPresent(p -> p.setLoreCounters(0));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter triggers

        // Resolve all chapter abilities on stack
        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        GameData gd = harness.getGameData();

        // Enchantments should not have any granted abilities
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .forEach(p -> assertThat(p.getUntilNextTurnActivatedAbilities()).isEmpty());
    }

    // ===== Chapter III: +1/+1 counters and keywords =====

    @Test
    @DisplayName("Chapter III puts +1/+1 counters on all creatures you control")
    void chapterIIIPutsCountersOnCreatures() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createVanillaCreature("Llanowar Elves", 1, 1));

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);

        Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElse(null);
        assertThat(elves).isNotNull();
        assertThat(elves.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III grants vigilance, trample, and indestructible until end of turn")
    void chapterIIIGrantsKeywords() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getGrantedKeywords()).contains(
                Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE
        );
    }

    @Test
    @DisplayName("Chapter III keywords are cleared at end of turn but counters persist")
    void chapterIIIKeywordsClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        // Advance to end step, then cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to cleanup (resets "until end of turn" modifiers)

        GameData gd = harness.getGameData();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();

        // Keywords should be cleared
        assertThat(bears.getGrantedKeywords()).doesNotContain(
                Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE
        );

        // +1/+1 counters should persist
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Saga lifecycle =====

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers
        harness.passBothPriorities(); // resolve chapter III

        GameData gd = harness.getGameData();

        // Saga should be sacrificed
        boolean sagaStillOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Song of Freyalise"));
        assertThat(sagaStillOnBf).isFalse();

        // Saga should be in graveyard
        boolean sagaInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Song of Freyalise"));
        assertThat(sagaInGraveyard).isTrue();
    }

    @Test
    @DisplayName("Saga is not sacrificed while chapter III ability is on the stack")
    void sagaNotSacrificedWhileChapterOnStack() {
        harness.addToBattlefield(player1, new SongOfFreyalise());
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Song of Freyalise"))
                .findFirst().orElse(null);
        assertThat(saga).isNotNull();
        saga.setLoreCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → lore counter 3, chapter III triggers

        GameData gd = harness.getGameData();

        assertThat(saga.getLoreCounters()).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        // Saga should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    // ===== Helper =====

    private Card createVanillaCreature(String name, int power, int toughness) {
        var token = new Card();
        token.setToken(true);
        token.setName(name);
        token.setPower(power);
        token.setToughness(toughness);
        token.setColor(CardColor.GREEN);
        token.setType(CardType.CREATURE);
        token.setSubtypes(List.of(CardSubtype.ELF));
        return token;
    }
}
