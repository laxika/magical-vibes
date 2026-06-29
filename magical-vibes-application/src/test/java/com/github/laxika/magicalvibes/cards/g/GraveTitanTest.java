package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraveTitanTest extends BaseCardTest {

    // ===== ETB trigger: create two 2/2 black Zombie tokens =====

    @Nested
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
    }

    // ===== Attack trigger: create two 2/2 black Zombie tokens =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Grave Titan creates two 2/2 Zombie tokens")
        void attackCreatesTwoZombieTokens() {
            Permanent graveTitan = addReadyGraveTitan(player1);

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
            Permanent graveTitan = addReadyGraveTitan(player1);

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
            Permanent graveTitan = addReadyGraveTitan(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve attack trigger

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            battlefield.stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                    .forEach(token -> {
                        assertThat(token.isAttackedThisTurn()).isFalse();
                    });
        }
    }

    // ===== Helpers =====

    private void castGraveTitan() {
        harness.setHand(player1, List.of(new GraveTitan()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyGraveTitan(Player player) {
        Permanent perm = new Permanent(new GraveTitan());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
