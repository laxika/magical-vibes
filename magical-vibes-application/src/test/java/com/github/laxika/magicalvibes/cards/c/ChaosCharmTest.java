package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.FemerefKnight;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WallOfRoots;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosCharm.class, WallOfRoots.class, BayFalcon.class, FeralShadow.class,
        FemerefKnight.class, Forest.class})
class ChaosCharmTest extends BaseCardTest {

    @Nested
    @CardUsed({ChaosCharm.class, WallOfRoots.class, FeralShadow.class})
    @DisplayName("Mode 0: Destroy target Wall")
    class DestroyWallMode {

        @Test
        @DisplayName("Destroys the targeted Wall")
        void destroysWall() {
            harness.addToBattlefield(player2, new WallOfRoots());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID wallId = harness.getPermanentId(player2, "Wall of Roots");
            harness.castInstant(player1, 0, 0, wallId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a non-Wall creature")
        void cannotTargetNonWall() {
            harness.addToBattlefield(player2, new FeralShadow());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, shadowId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({ChaosCharm.class, BayFalcon.class, FemerefKnight.class, Forest.class})
    @DisplayName("Mode 1: Deals 1 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Kills a 1-toughness creature")
        void killsOneToughnessCreature() {
            harness.addToBattlefield(player2, new BayFalcon());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID falconId = harness.getPermanentId(player2, "Bay Falcon");
            harness.castInstant(player1, 0, 1, falconId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreaturePermanent() {
            harness.addToBattlefield(player2, new Forest());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID forestId = harness.getPermanentId(player2, "Forest");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, forestId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Marks 1 damage on a larger creature")
        void marksDamage() {
            harness.addToBattlefield(player2, new FemerefKnight());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID knightId = harness.getPermanentId(player2, "Femeref Knight");
            harness.castInstant(player1, 0, 1, knightId);
            harness.passBothPriorities();

            assertThat(gqs.findPermanentById(gd, knightId).getMarkedDamage()).isEqualTo(1);
        }
    }

    @Nested
    @CardUsed({ChaosCharm.class, FeralShadow.class, Forest.class, BayFalcon.class})
    @DisplayName("Mode 2: Target creature gains haste until end of turn")
    class HasteMode {

        @Test
        @DisplayName("Haste lets a summoning-sick creature attack")
        void hasteAllowsImmediateAttack() {
            harness.addToBattlefield(player1, new FeralShadow());
            findPermanent(player1, "Feral Shadow").setSummoningSick(true);
            harness.addToBattlefield(player2, new BayFalcon());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player1, "Feral Shadow");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();
            declareAttackersAndPrepareBlockers(List.of(0));

            assertThat(findPermanent(player1, "Feral Shadow").isAttacking()).isTrue();
        }

        @Test
        @DisplayName("Can grant haste to an opponent's creature without affecting other creatures")
        void grantsHasteToOpponentsCreatureOnly() {
            harness.addToBattlefield(player2, new FeralShadow());
            harness.addToBattlefield(player1, new BayFalcon());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player2, "Feral Shadow");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Feral Shadow"), Keyword.HASTE)).isTrue();
            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Bay Falcon"), Keyword.HASTE)).isFalse();
        }

        @Test
        @DisplayName("Does not grant haste to another creature when its target leaves the battlefield")
        void targetLeavesBeforeResolution() {
            harness.addToBattlefield(player2, new FeralShadow());
            harness.addToBattlefield(player2, new BayFalcon());
            harness.setHand(player1, List.of(new ChaosCharm(), new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID targetId = harness.getPermanentId(player2, "Feral Shadow");
            harness.castInstant(player1, 0, 2, targetId);
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();
            harness.assertInGraveyard(player2, "Feral Shadow");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Bay Falcon"), Keyword.HASTE)).isFalse();
            harness.assertInGraveyard(player1, "Chaos Charm");
        }

        @Test
        @DisplayName("Grants haste to the target creature")
        void grantsHaste() {
            harness.addToBattlefield(player1, new FeralShadow());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player1, "Feral Shadow");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, gqs.findPermanentById(gd, targetId), Keyword.HASTE)).isTrue();
        }

        @Test
        @DisplayName("Haste wears off at end of turn")
        void hasteWearsOff() {
            harness.addToBattlefield(player1, new FeralShadow());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID targetId = harness.getPermanentId(player1, "Feral Shadow");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, gqs.findPermanentById(gd, targetId), Keyword.HASTE)).isFalse();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreaturePermanent() {
            harness.addToBattlefield(player1, new Forest());
            harness.setHand(player1, List.of(new ChaosCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID forestId = harness.getPermanentId(player1, "Forest");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, forestId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
