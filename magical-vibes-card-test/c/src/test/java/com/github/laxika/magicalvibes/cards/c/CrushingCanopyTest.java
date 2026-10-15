package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrushingCanopy.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class})
class CrushingCanopyTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 1: Destroy target creature with flying")
    @CardUsed({CrushingCanopy.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class})
    class DestroyFlyingCreatureMode {

        @Test
        @DisplayName("Destroys target creature with flying")
        void destroysFlyingCreature() {
            Permanent airElementalPermanent = harness.addToBattlefieldAndReturn(player2, new AirElemental());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0, 0, airElementalPermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Air Elemental");
            harness.assertInGraveyard(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetCreatureWithoutFlying() {
            Permanent bearsPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            // Need a valid target on the battlefield so spell is castable
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an enchantment with flying creature mode")
        void cannotTargetEnchantmentWithFlyingMode() {
            Permanent anthemPermanent = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
            // Need a valid target on the battlefield so spell is castable
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, anthemPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target enchantment")
    @CardUsed({CrushingCanopy.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class})
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            Permanent anthemPermanent = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0, 1, anthemPermanent.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
            harness.assertInGraveyard(player2, "Glorious Anthem");
        }

        @Test
        @DisplayName("Cannot target a creature without flying using enchantment mode")
        void cannotTargetCreatureWithEnchantmentMode() {
            Permanent bearsPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            // Need a valid target on the battlefield so spell is castable
            harness.addToBattlefield(player1, new GloriousAnthem());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        Permanent airElementalPermanent = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 99, airElementalPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Crushing Canopy goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent airElementalPermanent = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, airElementalPermanent.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Crushing Canopy");
    }

    @Test
    void canDestroyOwnFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    void enchantmentModeCannotTargetFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDestroyAnotherCreatureWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Crushing Canopy");
        assertThat(gd.stack).isEmpty();
    }
}
