package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AzoriusCharmTest extends BaseCardTest {

    private void addWU() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Creatures you control gain lifelink until end of turn")
    class LifelinkMode {

        @Test
        @DisplayName("Grants lifelink to your creatures")
        void grantsLifelink() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AzoriusCharm()));
            addWU();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        }

        @Test
        @DisplayName("Does not grant lifelink to opponent creatures")
        void doesNotGrantOpponentCreatures() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AzoriusCharm()));
            addWU();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            Permanent bears = findPermanent(player2, "Grizzly Bears");
            assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        }

        @Test
        @DisplayName("Lifelink wears off at end of turn")
        void wearsOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AzoriusCharm()));
            addWU();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 1: Draw a card")
    class DrawMode {

        @Test
        @DisplayName("Draws one card")
        void drawsOneCard() {
            harness.setHand(player1, List.of(new AzoriusCharm()));
            gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
            addWU();

            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Azorius Charm");
        }
    }

    @Nested
    @DisplayName("Mode 2: Put target attacking or blocking creature on top of library")
    class TuckMode {

        @Test
        @DisplayName("Puts attacking creature on top of owner's library")
        void tucksAttackingCreature() {
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            attacker.setAttackTarget(player1.getId());
            gd.playerBattlefields.get(player2.getId()).add(attacker);
            UUID targetId = attacker.getId();
            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

            harness.setHand(player1, List.of(new AzoriusCharm()));
            addWU();

            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            List<Card> deck = gd.playerDecks.get(player2.getId());
            assertThat(deck).hasSize(deckSizeBefore + 1);
            assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a non-attacking, non-blocking creature")
        void cannotTargetIdleCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID idleId = harness.getPermanentId(player2, "Grizzly Bears");
            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            attacker.setAttackTarget(player1.getId());
            gd.playerBattlefields.get(player2.getId()).add(attacker);

            harness.setHand(player1, List.of(new AzoriusCharm()));
            addWU();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, idleId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
