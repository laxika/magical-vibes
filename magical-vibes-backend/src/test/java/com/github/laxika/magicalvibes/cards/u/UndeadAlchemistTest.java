package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndeadAlchemistTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Undead Alchemist has the replacement and triggered effects")
    void hasCorrectEffects() {
        UndeadAlchemist card = new UndeadAlchemist();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof ReplaceCombatDamageWithMillEffect)
                .hasSize(1);

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED))
                .hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED).getFirst())
                .isInstanceOf(ExileMilledCreatureAndCreateTokenEffect.class);
    }

    // ===== Replacement effect: Zombie combat damage → mill =====

    @Nested
    @DisplayName("Replacement effect — Zombie combat damage → mill")
    class ReplacementEffect {

        @Test
        @DisplayName("Zombie combat damage is replaced with milling, no life loss")
        void zombieCombatDamageReplacedWithMill() {
            harness.addToBattlefield(player1, new UndeadAlchemist());
            harness.setLife(player2, 20);

            // Put an attacking Zombie creature (Undead Alchemist itself is 4/2 Zombie)
            Permanent attacker = new Permanent(new UndeadAlchemist());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);

            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // No life loss — damage is replaced
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            // 4 cards milled (attacker has 4 power)
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
        }

        @Test
        @DisplayName("Non-Zombie creature deals damage normally (no replacement)")
        void nonZombieCreatureDamageNotReplaced() {
            harness.addToBattlefield(player1, new UndeadAlchemist());
            harness.setLife(player2, 20);

            // GrizzlyBears is a 2/2 Bear, not a Zombie
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);

            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Life loss happens normally — non-Zombie
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            // No extra milling from replacement
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        }

        @Test
        @DisplayName("Undead Alchemist replaces its own combat damage (it is a Zombie)")
        void replacesOwnCombatDamage() {
            // Single Undead Alchemist on the battlefield, also attacking
            Permanent alchemist = new Permanent(new UndeadAlchemist());
            alchemist.setSummoningSick(false);
            alchemist.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(alchemist);

            harness.setLife(player2, 20);
            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // No life loss
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            // 4 cards milled (Undead Alchemist has 4 power)
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
        }
    }

    // ===== Triggered ability: creature card milled → exile + token =====

    @Nested
    @DisplayName("Triggered ability — creature card milled → exile + Zombie token")
    class TriggeredAbility {

        @Test
        @DisplayName("Milling a creature card exiles it and creates a Zombie token")
        void millingCreatureCardCreatesZombieToken() {
            Permanent alchemist = new Permanent(new UndeadAlchemist());
            alchemist.setSummoningSick(false);
            alchemist.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(alchemist);

            harness.setLife(player2, 20);

            // Set up opponent's deck: 4 creature cards at the top
            gd.playerDecks.get(player2.getId()).clear();
            for (int i = 0; i < 4; i++) {
                gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
            }

            int p1BattlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // 4 cards milled, all creatures → all exiled, 4 Zombie tokens created
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(4);
            // 4 new Zombie tokens created for player1
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(p1BattlefieldSizeBefore + 4);
            // Verify tokens are 2/2 black Zombies
            Permanent token = gd.playerBattlefields.get(player1.getId()).getLast();
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getName()).isEqualTo("Zombie");
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        }

        @Test
        @DisplayName("Milling non-creature cards does not create tokens")
        void millingNonCreatureCardsNoTokens() {
            Permanent alchemist = new Permanent(new UndeadAlchemist());
            alchemist.setSummoningSick(false);
            alchemist.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(alchemist);

            harness.setLife(player2, 20);

            // Set up opponent's deck: 4 non-creature cards (instants)
            gd.playerDecks.get(player2.getId()).clear();
            for (int i = 0; i < 4; i++) {
                gd.playerDecks.get(player2.getId()).add(new Shock());
            }

            int p1BattlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // 4 non-creature cards milled → go to graveyard, no tokens
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(p1BattlefieldSizeBefore);
        }

        @Test
        @DisplayName("Mixed deck: only creature cards trigger tokens, non-creatures go to graveyard")
        void mixedDeckOnlyCreaturesCreateTokens() {
            Permanent alchemist = new Permanent(new UndeadAlchemist());
            alchemist.setSummoningSick(false);
            alchemist.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(alchemist);

            harness.setLife(player2, 20);

            // Deck: creature, instant, creature, instant (milled top to bottom)
            gd.playerDecks.get(player2.getId()).clear();
            gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
            gd.playerDecks.get(player2.getId()).add(new Shock());
            gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
            gd.playerDecks.get(player2.getId()).add(new Shock());

            int p1BattlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // 4 cards milled: 2 creatures exiled + 2 tokens, 2 instants in graveyard
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
            assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(2);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(p1BattlefieldSizeBefore + 2);
        }
    }

    // ===== Multiple Undead Alchemists =====

    @Nested
    @DisplayName("Multiple Undead Alchemists interaction")
    class MultipleAlchemists {

        @Test
        @DisplayName("Two Undead Alchemists each create a token per creature card milled")
        void twoAlchemistsEachCreateTokens() {
            // Two Undead Alchemists on the battlefield
            harness.addToBattlefield(player1, new UndeadAlchemist());
            Permanent attacker = new Permanent(new UndeadAlchemist());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);

            harness.setLife(player2, 20);

            // Deck with 4 creature cards
            gd.playerDecks.get(player2.getId()).clear();
            for (int i = 0; i < 4; i++) {
                gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
            }

            int p1BattlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Each creature card triggers both Alchemists → 4 creatures × 2 triggers = 8 tokens
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(p1BattlefieldSizeBefore + 8);
        }
    }

    // ===== Replacement does not apply without Undead Alchemist =====

    @Test
    @DisplayName("Without Undead Alchemist, Zombie deals combat damage normally")
    void zombieDamageNormalWithoutAlchemist() {
        harness.setLife(player2, 20);

        // Use Unbreathing Horde or similar Zombie — but to keep it simple, use a
        // card with Zombie subtype. UndeadAlchemist itself is a Zombie but we test
        // without another Alchemist on the field to provide the replacement.
        // Create a plain zombie creature by manually setting up a card.
        Card zombieCard = new Card();
        zombieCard.setName("Test Zombie");
        zombieCard.setType(CardType.CREATURE);
        zombieCard.setSubtypes(List.of(CardSubtype.ZOMBIE));
        zombieCard.setPower(3);
        zombieCard.setToughness(2);
        zombieCard.setManaCost("{2}{B}");

        Permanent attacker = new Permanent(zombieCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Normal damage — 3 life lost, no milling
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Replacement is logged")
    void replacementIsLogged() {
        Permanent alchemist = new Permanent(new UndeadAlchemist());
        alchemist.setSummoningSick(false);
        alchemist.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(alchemist);

        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("combat damage is replaced with milling"));
    }
}
