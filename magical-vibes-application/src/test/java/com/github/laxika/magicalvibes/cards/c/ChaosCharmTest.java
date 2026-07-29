package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
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

class ChaosCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target Wall")
    class DestroyWallMode {

        @Test
        @DisplayName("Destroys the targeted Wall")
        void destroysWall() {
            harness.addToBattlefield(player2, new WallOfAir());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID wallId = harness.getPermanentId(player2, "Wall of Air");
            harness.castInstant(player1, 0, 0, wallId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a non-Wall creature")
        void cannotTargetNonWall() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Deals 1 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Kills a 1-toughness creature")
        void killsOneToughnessCreature() {
            harness.addToBattlefield(player2, new SavannahLions());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID lionsId = harness.getPermanentId(player2, "Savannah Lions");
            harness.castInstant(player1, 0, 1, lionsId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Marks 1 damage on a larger creature")
        void marksDamage() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, bearsId);
            harness.passBothPriorities();

            assertThat(permanent(player2.getId(), bearsId).getMarkedDamage()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature gains haste until end of turn")
    class HasteMode {

        @Test
        @DisplayName("Grants haste to the target creature")
        void grantsHaste() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(player1.getId(), targetId), Keyword.HASTE)).isTrue();
        }

        @Test
        @DisplayName("Haste wears off at end of turn")
        void hasteWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(player1.getId(), targetId), Keyword.HASTE)).isFalse();
        }
    }

    private Permanent permanent(UUID playerId, UUID id) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }
}
