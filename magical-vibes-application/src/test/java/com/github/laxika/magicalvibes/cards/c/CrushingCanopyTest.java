package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrushingCanopyTest extends BaseCardTest {

    @Test
    @DisplayName("Crushing Canopy has a ChooseOneEffect with two options")
    void hasCorrectEffects() {
        CrushingCanopy card = new CrushingCanopy();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.options()).hasSize(2);
    }

    @Nested
    @DisplayName("Mode 1: Destroy target creature with flying")
    class DestroyFlyingCreatureMode {

        @Test
        @DisplayName("Destroys target creature with flying")
        void destroysFlyingCreature() {
            AirElemental airElemental = new AirElemental();
            harness.addToBattlefield(player2, airElemental);

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent airElementalPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Air Elemental"))
                    .findFirst().orElseThrow();

            harness.castInstant(player1, 0, 0, airElementalPermanent.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Air Elemental"));
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetCreatureWithoutFlying() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);
            // Need a valid target on the battlefield so spell is castable
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent bearsPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an enchantment with flying creature mode")
        void cannotTargetEnchantmentWithFlyingMode() {
            GloriousAnthem anthem = new GloriousAnthem();
            harness.addToBattlefield(player2, anthem);
            // Need a valid target on the battlefield so spell is castable
            harness.addToBattlefield(player1, new AirElemental());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent anthemPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Glorious Anthem"))
                    .findFirst().orElseThrow();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, anthemPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            GloriousAnthem anthem = new GloriousAnthem();
            harness.addToBattlefield(player2, anthem);

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent anthemPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Glorious Anthem"))
                    .findFirst().orElseThrow();

            harness.castInstant(player1, 0, 1, anthemPermanent.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Glorious Anthem"));
        }

        @Test
        @DisplayName("Cannot target a creature without flying using enchantment mode")
        void cannotTargetCreatureWithEnchantmentMode() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);
            // Need a valid target on the battlefield so spell is castable
            harness.addToBattlefield(player1, new GloriousAnthem());

            harness.setHand(player1, List.of(new CrushingCanopy()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            Permanent bearsPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        AirElemental airElemental = new AirElemental();
        harness.addToBattlefield(player2, airElemental);

        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent airElementalPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 99, airElementalPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Crushing Canopy goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        AirElemental airElemental = new AirElemental();
        harness.addToBattlefield(player2, airElemental);

        harness.setHand(player1, List.of(new CrushingCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent airElementalPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();

        harness.castInstant(player1, 0, 0, airElementalPermanent.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crushing Canopy"));
    }
}
