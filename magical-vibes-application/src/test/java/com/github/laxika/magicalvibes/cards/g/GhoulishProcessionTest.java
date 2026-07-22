package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GhoulishProcessionTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("Creates a decayed Zombie when a nontoken creature dies")
        void createsDecayedZombieOnNontokenDeath() {
            harness.addToBattlefield(player1, new GhoulishProcession());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player2, 0, bearsId);
            harness.passBothPriorities(); // Resolve Shock
            harness.passBothPriorities(); // Resolve Procession trigger

            GameData gd = harness.getGameData();
            Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .findFirst().orElseThrow();

            assertThat(zombie.getCard().isToken()).isTrue();
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
            assertThat(zombie.getCard().getEffects(EffectSlot.STATIC))
                    .anyMatch(CantBlockEffect.class::isInstance);
            assertThat(zombie.getCard().getEffects(EffectSlot.ON_ATTACK))
                    .anyMatch(SacrificeAtEndOfCombatEffect.class::isInstance);
            assertThat(gqs.canBlock(gd, zombie)).isFalse();
        }

        @Test
        @DisplayName("Triggers only once when multiple nontoken creatures die to the same Wrath")
        void triggersOnlyOnceForSimultaneousDeaths() {
            harness.addToBattlefield(player1, new GhoulishProcession());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities(); // Resolve Procession trigger

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not trigger again later in the same turn")
        void doesNotTriggerAgainSameTurn() {
            harness.addToBattlefield(player1, new GhoulishProcession());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(new Shock(), new Shock()));
            harness.addMana(player2, ManaColor.RED, 2);

            UUID firstBearId = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .map(Permanent::getId)
                    .findFirst().orElseThrow();
            harness.castInstant(player2, 0, firstBearId);
            harness.passBothPriorities(); // Resolve Shock
            harness.passBothPriorities(); // Resolve Procession trigger

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .count()).isEqualTo(1);

            UUID secondBearId = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .map(Permanent::getId)
                    .findFirst().orElseThrow();
            harness.castInstant(player2, 0, secondBearId);
            harness.passBothPriorities(); // Resolve Shock

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Token creature deaths do not trigger")
        void tokenDeathDoesNotTrigger() {
            harness.addToBattlefield(player1, new GhoulishProcession());
            Card tokenBear = new GrizzlyBears();
            tokenBear.setToken(true);
            harness.addToBattlefield(player1, tokenBear);

            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player2, 0, bearsId);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Zombie"));
        }

        @Test
        @DisplayName("Triggers again on a later turn")
        void triggersAgainNextTurn() {
            harness.addToBattlefield(player1, new GhoulishProcession());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            UUID firstBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player2, 0, firstBearId);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .count()).isEqualTo(1);

            advanceTurn();
            advanceTurn(); // back to a fresh turn where Procession can trigger again

            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            UUID secondBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player2, 0, secondBearId);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Decayed Zombie combat")
    class DecayedCombatTests {

        @Test
        @DisplayName("Decayed Zombie is sacrificed at end of combat after attacking")
        void sacrificedAtEndOfCombatWhenAttacking() {
            harness.addToBattlefield(player1, new GhoulishProcession());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player2, 0, bearsId);
            harness.passBothPriorities();
            harness.passBothPriorities();

            Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Zombie"))
                    .findFirst().orElseThrow();
            zombie.setSummoningSick(false);

            harness.setLife(player2, 20);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();

            int zombieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);
            gs.declareAttackers(gd, player1, List.of(zombieIndex));
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Zombie"));
        }
    }
}
