package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.ForbiddenAlchemy;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndeadAlchemist.class, GrizzlyBears.class, Shock.class})
class UndeadAlchemistTest extends BaseCardTest {


    @Nested
    @DisplayName("Replacement effect — Zombie combat damage → mill")
    @CardUsed({UndeadAlchemist.class, GrizzlyBears.class})
    class ReplacementEffect {

        @Test
        @DisplayName("Zombie combat damage is replaced with milling, no life loss")
        void zombieCombatDamageReplacedWithMill() {
            harness.addToBattlefield(player1, new UndeadAlchemist());
            harness.setLife(player2, 20);

            // Put an attacking Zombie creature (Undead Alchemist itself is 4/2 Zombie)
            Permanent attacker = addCreatureReady(player1, new UndeadAlchemist());
            attacker.setAttacking(true);

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
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);

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
            Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
            alchemist.setAttacking(true);

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


    @Nested
    @DisplayName("Triggered ability — creature card milled → exile + Zombie token")
    @CardUsed({UndeadAlchemist.class, GrizzlyBears.class, Shock.class})
    class TriggeredAbility {

        @Test
        @DisplayName("Milling a creature card exiles it and creates a Zombie token")
        void millingCreatureCardCreatesZombieToken() {
            Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
            alchemist.setAttacking(true);

            harness.setLife(player2, 20);

            // Set up opponent's deck: 4 creature cards at the top
            harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

            int p1BattlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // 4 cards milled, all creatures → all exiled, 4 Zombie tokens created
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(4);
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
            Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
            alchemist.setAttacking(true);

            harness.setLife(player2, 20);

            // Set up opponent's deck: 4 non-creature cards (instants)
            harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));

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
            Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
            alchemist.setAttacking(true);

            harness.setLife(player2, 20);

            // Deck: creature, instant, creature, instant (milled top to bottom)
            harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock()));

            int p1BattlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // 4 cards milled: 2 creatures exiled + 2 tokens, 2 instants in graveyard
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
            assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(p1BattlefieldSizeBefore + 2);
        }
    }


    @Nested
    @DisplayName("Multiple Undead Alchemists interaction")
    @CardUsed({UndeadAlchemist.class, GrizzlyBears.class})
    class MultipleAlchemists {

        @Test
        @DisplayName("Two Undead Alchemists each create a token per creature card milled")
        void twoAlchemistsEachCreateTokens() {
            // Two Undead Alchemists on the battlefield
            harness.addToBattlefield(player1, new UndeadAlchemist());
            Permanent attacker = addCreatureReady(player1, new UndeadAlchemist());
            attacker.setAttacking(true);

            harness.setLife(player2, 20);

            // Deck with 4 creature cards
            harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

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

        Permanent attacker = addCreatureReady(player1, zombieCard);
        attacker.setAttacking(true);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Normal damage — 3 life lost, no milling
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }


    @Test
    @DisplayName("Replacement is logged")
    void replacementIsLogged() {
        Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
            alchemist.setAttacking(true);

        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("combat damage is replaced with milling"));
    }

    @Test
    void creatureMillWaitsForTriggeredAbilityResolution() {
        Permanent attacker = addCreatureReady(player1, new UndeadAlchemist());
        attacker.setAttacking(true);
        Card creature = new UndeadAlchemist();
        harness.setLibrary(player2, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(attacker);
    }

    @Test
    @CardUsed({UndeadAlchemist.class, ForbiddenAlchemy.class})
    void creaturesPutIntoGraveyardByForbiddenAlchemyAlsoCreateTokens() {
        harness.addToBattlefield(player1, new UndeadAlchemist());
        Card chosen = new ForbiddenAlchemy();
        harness.setLibrary(player2, List.of(chosen, new UndeadAlchemist(),
                new UndeadAlchemist(), new UndeadAlchemist()));
        harness.setHand(player2, List.of(new ForbiddenAlchemy()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0);
        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));
        for (int i = 0; i < 3 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(3);
    }

}
