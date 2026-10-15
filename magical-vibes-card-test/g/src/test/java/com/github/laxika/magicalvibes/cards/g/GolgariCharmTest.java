package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.s.SewerShambler;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({GolgariCharm.class, DrudgeBeetle.class, GrowingRanks.class, SewerShambler.class, UltimatePrice.class})
class GolgariCharmTest extends BaseCardTest {

    private void addBG() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Nested
    @DisplayName("Mode 0: All creatures get -1/-1 until end of turn")
    @CardUsed({GolgariCharm.class, DrudgeBeetle.class, SewerShambler.class})
    class DebuffMode {

        @Test
        @DisplayName("Debuffs creatures on both sides")
        void debuffsAllCreatures() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.addToBattlefield(player2, new DrudgeBeetle());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            Permanent own = findPermanent(player1, "Drudge Beetle");
            Permanent opp = findPermanent(player2, "Drudge Beetle");
            assertThat(own.getPowerModifier()).isEqualTo(-1);
            assertThat(own.getToughnessModifier()).isEqualTo(-1);
            assertThat(opp.getPowerModifier()).isEqualTo(-1);
            assertThat(opp.getToughnessModifier()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Debuff wears off at end of turn")
        void wearsOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Drudge Beetle");
            assertThat(bears.getPowerModifier()).isEqualTo(0);
            assertThat(bears.getToughnessModifier()).isEqualTo(0);
        }

        @Test
        void killsOneToughnessCreaturesDespiteRegeneration() {
            harness.addToBattlefield(player1, new SewerShambler());
            harness.addToBattlefield(player2, new SewerShambler());
            harness.setHand(player1, List.of(new GolgariCharm(), new GolgariCharm()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();
            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Sewer Shambler");
            harness.assertInGraveyard(player2, "Sewer Shambler");
            harness.assertNotOnBattlefield(player1, "Sewer Shambler");
            harness.assertNotOnBattlefield(player2, "Sewer Shambler");
        }

        @Test
        void doesNotDebuffCreaturesEnteringAfterResolution() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();
            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.addToBattlefield(player2, new DrudgeBeetle());

            assertThat(findPermanent(player1, "Drudge Beetle").getToughnessModifier()).isEqualTo(-1);
            assertThat(findPermanent(player2, "Drudge Beetle").getToughnessModifier()).isZero();
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    @CardUsed({GolgariCharm.class, GrowingRanks.class, DrudgeBeetle.class})
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GrowingRanks());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            UUID targetId = harness.getPermanentId(player2, "Growing Ranks");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Growing Ranks");
            harness.assertInGraveyard(player2, "Growing Ranks");
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new DrudgeBeetle());
            harness.addToBattlefield(player1, new GrowingRanks());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            UUID targetId = harness.getPermanentId(player2, "Drudge Beetle");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Regenerate each creature you control")
    @CardUsed({GolgariCharm.class, DrudgeBeetle.class, GrowingRanks.class, UltimatePrice.class})
    class RegenerateMode {

        @Test
        @DisplayName("Gives regeneration shields to your creatures only")
        void regeneratesOwnCreatures() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.addToBattlefield(player2, new DrudgeBeetle());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent own = findPermanent(player1, "Drudge Beetle");
            Permanent opp = findPermanent(player2, "Drudge Beetle");
            assertThat(own.getRegenerationShield()).isGreaterThan(0);
            assertThat(opp.getRegenerationShield()).isEqualTo(0);
        }

        @Test
        void shieldPreventsOnlyOneDestructionAndDoesNotTapImmediately() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.setHand(player1, List.of(new GolgariCharm()));
            harness.setHand(player2, List.of(new UltimatePrice(), new UltimatePrice()));
            addBG();
            harness.addMana(player2, ManaColor.BLACK, 4);
            UUID targetId = harness.getPermanentId(player1, "Drudge Beetle");

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent beetle = findPermanent(player1, "Drudge Beetle");
            assertThat(beetle.isTapped()).isFalse();
            harness.castAndResolveInstant(player2, 0, targetId);
            harness.assertOnBattlefield(player1, "Drudge Beetle");
            harness.assertNotInGraveyard(player1, "Drudge Beetle");
            assertThat(beetle.isTapped()).isTrue();
            assertThat(beetle.getRegenerationShield()).isZero();

            harness.castAndResolveInstant(player2, 0, targetId);
            harness.assertNotOnBattlefield(player1, "Drudge Beetle");
            harness.assertInGraveyard(player1, "Drudge Beetle");
        }

        @Test
        void doesNotShieldNoncreaturesOrCreaturesEnteringLater() {
            harness.addToBattlefield(player1, new GrowingRanks());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();
            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            harness.addToBattlefield(player1, new DrudgeBeetle());

            assertThat(findPermanent(player1, "Growing Ranks").getRegenerationShield()).isZero();
            assertThat(findPermanent(player1, "Drudge Beetle").getRegenerationShield()).isZero();
            harness.assertInGraveyard(player1, "Golgari Charm");
        }

        @Test
        void unusedShieldExpiresAtEndOfTurn() {
            harness.addToBattlefield(player1, new DrudgeBeetle());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();
            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();
            assertThat(findPermanent(player1, "Drudge Beetle").getRegenerationShield()).isEqualTo(1);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(findPermanent(player1, "Drudge Beetle").getRegenerationShield()).isZero();
        }
    }
}
