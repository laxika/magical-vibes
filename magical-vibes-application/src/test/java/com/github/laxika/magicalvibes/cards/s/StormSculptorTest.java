package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormSculptorTest extends BaseCardTest {

    // ===== Effect structure =====

    @Test
    @DisplayName("Has CantBeBlockedEffect as static effect")
    void hasStaticCantBeBlockedEffect() {
        StormSculptor card = new StormSculptor();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CantBeBlockedEffect.class);
    }

    @Test
    @DisplayName("Has ReturnTargetPermanentToHandEffect as ETB effect")
    void hasEtbBounceEffect() {
        StormSculptor card = new StormSculptor();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ReturnTargetPermanentToHandEffect.class);
    }

    // ===== ETB bounce =====

    @Nested
    @DisplayName("ETB bounce creature you control")
    class EtbBounce {

        @Test
        @DisplayName("ETB trigger goes on the stack when Storm Sculptor enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            castStormSculptor(player1, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Sculptor");
        }

        @Test
        @DisplayName("ETB resolves: target creature is returned to owner's hand")
        void etbBouncesTargetCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            castStormSculptor(player1, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Storm Sculptor remains on battlefield after bouncing another creature")
        void sculptorRemainsAfterBounce() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            castStormSculptor(player1, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Storm Sculptor"));
        }

        @Test
        @DisplayName("Stack is empty after full resolution")
        void stackEmptyAfterResolution() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            castStormSculptor(player1, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== Targeting restrictions =====

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target opponent's creature")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player1, List.of(new StormSculptor()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            assertThatThrownBy(() ->
                    harness.getGameService().playCard(gd, player1, 0, 0, opponentCreatureId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Can't be blocked =====

    @Nested
    @DisplayName("Can't be blocked")
    class CantBeBlocked {

        @Test
        @DisplayName("Storm Sculptor cannot be blocked")
        void cannotBeBlocked() {
            Permanent blockerPerm = new Permanent(new GrizzlyBears());
            blockerPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

            Permanent atkPerm = new Permanent(new StormSculptor());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be blocked");
        }

        @Test
        @DisplayName("Unblocked Storm Sculptor deals 3 damage to defending player")
        void dealsThreeDamageWhenUnblocked() {
            harness.setLife(player2, 20);

            Permanent atkPerm = new Permanent(new StormSculptor());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }
    }

    // ===== Helpers =====

    private void castStormSculptor(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StormSculptor()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }

}
