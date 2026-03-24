package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamcallerSirenTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has blocking restriction static effect")
    void hasBlockingRestrictionEffect() {
        DreamcallerSiren card = new DreamcallerSiren();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CanBlockOnlyIfAttackerMatchesPredicateEffect.class);
    }

    @Test
    @DisplayName("Has controls-another-Pirate conditional ETB tap effect")
    void hasConditionalEtbEffect() {
        DreamcallerSiren card = new DreamcallerSiren();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ControlsAnotherSubtypeConditionalEffect.class);

        ControlsAnotherSubtypeConditionalEffect conditional =
                (ControlsAnotherSubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(conditional.subtypes()).isEqualTo(Set.of(CardSubtype.PIRATE));
        assertThat(conditional.wrapped()).isInstanceOf(TapTargetPermanentEffect.class);
    }

    // ===== ETB with another Pirate =====

    @Nested
    @DisplayName("ETB with another Pirate")
    class EtbWithAnotherPirate {

        @Test
        @DisplayName("ETB triggers when you control another Pirate — taps two targets")
        void etbTapsTwoTargets() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate

            Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assertThat(bears1.isTapped()).isFalse();
            assertThat(bears2.isTapped()).isFalse();

            castDreamcallerSiren(List.of(bears1.getId(), bears2.getId()));
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears1.isTapped()).isTrue();
            assertThat(bears2.isTapped()).isTrue();
        }

        @Test
        @DisplayName("ETB triggers when you control another Pirate — taps one target")
        void etbTapsOneTarget() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate

            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            assertThat(bears.isTapped()).isFalse();

            castDreamcallerSiren(List.of(bears.getId()));
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("ETB trigger goes on the stack")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player1, new DaringSaboteur()); // Pirate
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            castDreamcallerSiren(List.of(bearsId));
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dreamcaller Siren");
        }

        @Test
        @DisplayName("Creature enters battlefield with another Pirate")
        void creatureEntersWithAnotherPirate() {
            harness.addToBattlefield(player1, new DaringSaboteur());
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            castDreamcallerSiren(List.of(bearsId));
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Dreamcaller Siren");
        }
    }

    // ===== ETB without another Pirate =====

    @Nested
    @DisplayName("ETB without another Pirate")
    class EtbWithoutAnotherPirate {

        @Test
        @DisplayName("ETB does NOT trigger without another Pirate (only self)")
        void etbDoesNotTriggerWithoutAnotherPirate() {
            castDreamcallerSiren(List.of());
            harness.passBothPriorities(); // resolve creature spell

            // No ETB trigger on the stack
            assertThat(gd.stack).isEmpty();

            // Creature is still on the battlefield
            harness.assertOnBattlefield(player1, "Dreamcaller Siren");
        }

        @Test
        @DisplayName("ETB does NOT trigger when opponent controls a Pirate but you don't")
        void etbDoesNotTriggerWithOpponentPirate() {
            harness.addToBattlefield(player2, new DaringSaboteur());

            castDreamcallerSiren(List.of());
            harness.passBothPriorities(); // resolve creature spell

            // No ETB trigger
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== Condition lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if the other Pirate is removed before resolution")
    void etbFizzlesWhenAnotherPirateRemoved() {
        harness.addToBattlefield(player1, new DaringSaboteur());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDreamcallerSiren(List.of(bears.getId()));
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the other Pirate before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Daring Saboteur"));

        harness.passBothPriorities(); // resolve ETB trigger — condition no longer met

        // Target should NOT be tapped (ability does nothing)
        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Blocking restriction =====

    @Nested
    @DisplayName("Blocking restriction")
    class BlockingRestriction {

        @Test
        @DisplayName("Can block a creature with flying")
        void canBlockFlyingCreature() {
            Permanent sirenPerm = new Permanent(new DreamcallerSiren());
            sirenPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(sirenPerm);

            Permanent atkPerm = new Permanent(new AirElemental());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(sirenPerm.isBlocking()).isTrue();
        }

        @Test
        @DisplayName("Cannot block a creature without flying")
        void cannotBlockNonFlyingCreature() {
            Permanent sirenPerm = new Permanent(new DreamcallerSiren());
            sirenPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(sirenPerm);

            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can only block creatures with flying");
        }
    }

    // ===== Helpers =====

    private void castDreamcallerSiren(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new DreamcallerSiren()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, targetIds);
    }
}
