package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestructiveTampering.class, Millstone.class, GrizzlyBears.class, AirElemental.class})
class DestructiveTamperingTest extends BaseCardTest {

    private void castSpell(int modeIndex, UUID targetId) {
        harness.setHand(player1, List.of(new DestructiveTampering()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, modeIndex, targetId);
        harness.passBothPriorities();
    }

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    @CardUsed({DestructiveTampering.class, Millstone.class, GrizzlyBears.class})
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
            castSpell(0, millstone.getId());

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact permanent")
        void cannotTargetNonArtifact() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new DestructiveTampering()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Creatures without flying can't block this turn")
    @CardUsed({DestructiveTampering.class, GrizzlyBears.class, AirElemental.class})
    class CantBlockMode {
        @Test
        @DisplayName("Creatures entering after resolution cannot block")
        void laterGroundCreatureCannotBlock() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            attacker.setSummoningSick(false);
            castSpell(1, null);
            harness.addToBattlefield(player2, new GrizzlyBears());
            attacker.setAttacking(true);
            prepareDeclareBlockers();
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Restriction applies to the caster's creatures too")
        void castersGroundCreatureCannotBlock() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            castSpell(1, null);
            attacker.setAttacking(true);
            prepareDeclareBlockers(player2);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Ground creatures cannot block")
        void groundCreaturesCannotBlock() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            attacker.setSummoningSick(false);
            Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            blocker.setSummoningSick(false);

            castSpell(1, null);

            attacker.setAttacking(true);
            prepareDeclareBlockers();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Flying creatures can still block")
        void flyingCreaturesCanStillBlock() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            attacker.setSummoningSick(false);
            Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AirElemental());
            blocker.setSummoningSick(false);

            castSpell(1, null);

            attacker.setAttacking(true);
            prepareDeclareBlockers();

            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(blocker.isBlocking()).isTrue();
        }
    }
}
