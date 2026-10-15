package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveTitan.class, Unsummon.class, DuskdaleWurm.class})
class GraveTitanTest extends BaseCardTest {

    @Nested
    @CardUsed({GraveTitan.class})
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("Casting Grave Titan creates two 2/2 Zombie tokens on ETB")
        void etbCreatesTwoZombieTokens() {
            castGraveTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            long zombieTokenCount = battlefield.stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                    .count();
            assertThat(zombieTokenCount).isEqualTo(2);
        }

        @Test
        @DisplayName("ETB Zombie tokens are 2/2 black Zombies")
        void etbTokenCharacteristics() {
            castGraveTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            battlefield.stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                    .forEach(token -> {
                        assertThat(token.getCard().getPower()).isEqualTo(2);
                        assertThat(token.getCard().getToughness()).isEqualTo(2);
                        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
                    });
        }

        @Test
        @DisplayName("Entering without being cast gives tokens to Titan's controller")
        void enteringWithoutCastingCreatesTokensForController() {
            harness.enterBattlefieldAndReturn(player2, new GraveTitan());

            assertThat(countPermanents(player2, "Zombie")).isZero();
            harness.passBothPriorities();

            assertThat(findPermanents(player2, "Zombie")).hasSize(2);
            assertThat(countPermanents(player1, "Zombie")).isZero();
        }
    }

    @Nested
    @CardUsed({GraveTitan.class, Unsummon.class})
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Grave Titan creates two 2/2 Zombie tokens")
        void attackCreatesTwoZombieTokens() {
            addCreatureReady(player1, new GraveTitan());

            declareAttackers(List.of(0));

            // Resolve attack trigger
            harness.passBothPriorities();

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            long zombieTokenCount = battlefield.stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                    .count();
            assertThat(zombieTokenCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Attack Zombie tokens are 2/2 black Zombies")
        void attackTokenCharacteristics() {
            addCreatureReady(player1, new GraveTitan());

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve attack trigger

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            battlefield.stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                    .forEach(token -> {
                        assertThat(token.getCard().getPower()).isEqualTo(2);
                        assertThat(token.getCard().getToughness()).isEqualTo(2);
                        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
                    });
        }

        @Test
        @DisplayName("Attack tokens are NOT tapped and attacking (unlike Hero of Bladehold)")
        void attackTokensAreNotTappedAndAttacking() {
            addCreatureReady(player1, new GraveTitan());

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve attack trigger

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            battlefield.stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                    .forEach(token -> {
                        assertThat(token.isTapped()).isFalse();
                        assertThat(token.isAttackedThisTurn()).isFalse();
                    });
        }

        @Test
        @DisplayName("Attack trigger creates tokens after Titan returns to hand")
        void attackTriggerSurvivesSourceLeaving() {
            Permanent titan = addCreatureReady(player1, new GraveTitan());
            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            declareAttackers(List.of(0));
            assertThat(countPermanents(player1, "Zombie")).isZero();

            harness.castAndResolveInstant(player2, 0, titan.getId());
            assertThat(countPermanents(player1, "Grave Titan")).isZero();
            harness.passBothPriorities();

            assertThat(findPermanents(player1, "Zombie")).hasSize(2);
            assertThat(countPermanents(player2, "Zombie")).isZero();
        }
    }

    @Test
    @CardUsed({GraveTitan.class, DuskdaleWurm.class})
    @DisplayName("Deathtouch destroys a blocker with more toughness than Titan's power")
    void deathtouchKillsLargerBlocker() {
        addCreatureReady(player1, new GraveTitan());
        harness.addToBattlefield(player2, new DuskdaleWurm());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(countPermanents(player2, "Duskdale Wurm")).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof DuskdaleWurm);
    }

    private void castGraveTitan() {
        harness.castFromHand(player1, new GraveTitan(), "{4}{B}{B}");
    }
}
