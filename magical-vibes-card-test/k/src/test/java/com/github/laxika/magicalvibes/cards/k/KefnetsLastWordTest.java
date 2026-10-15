package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Annex;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KefnetsLastWord.class, Annex.class, GrizzlyBears.class, Island.class, Ornithopter.class, Pacifism.class, Plains.class})
class KefnetsLastWordTest extends BaseCardTest {

    @Nested
    @CardUsed({KefnetsLastWord.class, GrizzlyBears.class, Ornithopter.class, Pacifism.class, Plains.class})
    @DisplayName("Gain control of target artifact, creature, or enchantment")
    class GainControl {

        @Test
        @DisplayName("Gains control of a target creature")
        void gainsControlOfCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            cast(target);

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Gains control of a target artifact")
        void gainsControlOfArtifact() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

            cast(target);

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Gains control of a target enchantment")
        void gainsControlOfEnchantment() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
            pacifism.setAttachedTo(bears.getId());

            cast(pacifism);

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(pacifism.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(pacifism.getId()));
        }

        @Test
        @DisplayName("Control change is permanent — the creature is still yours after end of turn")
        void controlIsPermanent() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            cast(target);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Cannot target a land")
        void cannotTargetLand() {
            harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // a legal target so the spell is playable
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
            harness.setHand(player1, List.of(new KefnetsLastWord()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be an artifact, creature, or enchantment");
        }
    }

    @Nested
    @CardUsed({KefnetsLastWord.class, Annex.class, GrizzlyBears.class, Island.class, Plains.class})
    @DisplayName("Lands you control don't untap during your next untap step")
    class LandsDontUntap {

        @Test
        @DisplayName("Controller's lands stay tapped during the next untap step")
        void marksControllerLands() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
            plains.tap();
            island.tap();

            cast(target);

            harness.performUntapStep(player1);

            assertThat(plains.isTapped()).isTrue();
            assertThat(island.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Opponent's lands are unaffected")
        void opponentLandsUnaffected() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
            opponentPlains.tap();

            cast(target);

            assertThat(opponentPlains.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Lands entering after resolution also stay tapped during the next untap step")
        void laterLandsDontUntap() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            cast(target);
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
            land.tap();

            harness.performUntapStep(player1);

            assertThat(land.isTapped()).isTrue();
            harness.performUntapStep(player1);
            assertThat(land.isTapped()).isFalse();
        }

        @Test
        @CardUsed({Annex.class})
        @DisplayName("A land gained by the opponent after resolution untaps for that opponent")
        void restrictionDoesNotFollowLandsToAnotherController() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
            land.tap();
            cast(target);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player2, List.of(new Annex()));
            harness.addMana(player2, ManaColor.BLUE, 4);
            harness.castEnchantment(player2, 0, land.getId());
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(land.getId()));
            harness.performUntapStep(player2);

            assertThat(land.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Lands untapped at resolution but tapped later also skip the next untap")
        void initiallyUntappedLandsDontUntap() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
            cast(target);
            land.tap();

            harness.performUntapStep(player1);

            assertThat(land.isTapped()).isTrue();
            harness.performUntapStep(player1);
            assertThat(land.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Multiple resolutions only prevent untapping during the same next untap step")
        void multipleResolutionsExpireTogether() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
            land.tap();
            cast(target);
            cast(target);

            harness.performUntapStep(player1);
            assertThat(land.isTapped()).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(target.getId()));
            harness.performUntapStep(player1);
            assertThat(land.isTapped()).isFalse();
        }

        @Test
        @DisplayName("An illegal target prevents the entire spell from resolving, including the untap restriction")
        void illegalTargetDoesNotRestrictUntapping() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
            land.tap();
            harness.setHand(player1, List.of(new KefnetsLastWord()));
            harness.addMana(player1, ManaColor.BLUE, 4);
            harness.castSorcery(player1, 0, target.getId());
            gd.playerBattlefields.get(player2.getId()).remove(target);
            gd.playerGraveyards.get(player2.getId()).add(target.getCard());

            harness.passBothPriorities();
            harness.performUntapStep(player1);

            assertThat(land.isTapped()).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(target.getId()));
        }
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new KefnetsLastWord()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
