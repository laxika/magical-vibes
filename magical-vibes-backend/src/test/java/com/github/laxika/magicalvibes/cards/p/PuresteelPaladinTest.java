package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PuresteelPaladinTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has equipment entering triggered MayEffect(DrawCardEffect)")
    void hasEquipmentEntersTrigger() {
        PuresteelPaladin card = new PuresteelPaladin();

        var effects = card.getEffects(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) effects.getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Has metalcraft static effect wrapping GrantActivatedAbilityEffect")
    void hasMetalcraftEquipEffect() {
        PuresteelPaladin card = new PuresteelPaladin();

        var effects = card.getEffects(EffectSlot.STATIC);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(MetalcraftConditionalEffect.class);
        MetalcraftConditionalEffect mc = (MetalcraftConditionalEffect) effects.getFirst();
        assertThat(mc.wrapped()).isInstanceOf(GrantActivatedAbilityEffect.class);
    }

    // ===== Equipment entering draw trigger =====

    @Nested
    @DisplayName("Equipment entering draw trigger")
    class EquipmentEnteringDrawTrigger {

        @Test
        @DisplayName("Casting equipment triggers may-draw when Paladin is on battlefield")
        void castingEquipmentTriggersMayDraw() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new LeoninScimitar()));
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // resolve equipment spell, equipment enters, trigger goes on stack
            harness.passBothPriorities(); // resolve trigger (MayEffect -> prompt)

            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may-draw draws a card")
        void acceptingMayDrawDrawsCard() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new LeoninScimitar()));
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // resolve equipment spell
            harness.passBothPriorities(); // resolve trigger

            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities(); // resolve DrawCardEffect

            int deckSizeAfter = gd.playerDecks.get(player1.getId()).size();
            assertThat(deckSizeAfter).isEqualTo(deckSizeBefore - 1);
        }

        @Test
        @DisplayName("Declining may-draw does not draw a card")
        void decliningMayDrawDoesNotDraw() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new LeoninScimitar()));
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // resolve equipment spell
            harness.passBothPriorities(); // resolve trigger
            harness.handleMayAbilityChosen(player1, false);

            // Hand should be empty: equipment was cast from hand and draw was declined
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Non-equipment artifact entering does not trigger draw")
        void nonEquipmentArtifactDoesNotTrigger() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new PristineTalisman()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // resolve artifact spell

            // No may ability prompt since PristineTalisman is not equipment
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        }

        @Test
        @DisplayName("Trigger does not fire for opponent's equipment entering")
        void doesNotTriggerForOpponentEquipment() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player2, List.of(new LeoninScimitar()));
            harness.addMana(player2, ManaColor.COLORLESS, 1);

            harness.forceActivePlayer(player2);
            harness.castArtifact(player2, 0);
            harness.passBothPriorities(); // resolve equipment spell

            // Paladin only triggers for equipment under your control
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        }
    }

    // ===== Metalcraft equip {0} =====

    @Nested
    @DisplayName("Metalcraft equip {0}")
    class MetalcraftEquipZero {

        @Test
        @DisplayName("Equipment gains equip {0} when metalcraft is active")
        void equipmentGainsEquipZeroWithMetalcraft() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            LeoninScimitar scimitar = new LeoninScimitar();
            harness.addToBattlefield(player1, scimitar);
            // Need 3 artifacts for metalcraft — add two more
            harness.addToBattlefield(player1, new DarksteelAxe());
            harness.addToBattlefield(player1, new PristineTalisman());

            Permanent scimitarPerm = findPermanent(player1, "Leonin Scimitar");
            GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, scimitarPerm);

            assertThat(bonus.grantedActivatedAbilities()).hasSize(1);
            assertThat(bonus.grantedActivatedAbilities().getFirst().getManaCost()).isEqualTo("{0}");
        }

        @Test
        @DisplayName("Equipment does NOT gain equip {0} without metalcraft")
        void equipmentDoesNotGainEquipZeroWithoutMetalcraft() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            LeoninScimitar scimitar = new LeoninScimitar();
            harness.addToBattlefield(player1, scimitar);
            // Only 1 artifact (scimitar), metalcraft not met

            Permanent scimitarPerm = findPermanent(player1, "Leonin Scimitar");
            GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, scimitarPerm);

            assertThat(bonus.grantedActivatedAbilities()).isEmpty();
        }

        @Test
        @DisplayName("Equip {0} can be activated to attach equipment to creature")
        void equipZeroCanAttachToCreature() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.addToBattlefield(player1, new GrizzlyBears());
            LeoninScimitar scimitar = new LeoninScimitar();
            harness.addToBattlefield(player1, scimitar);
            harness.addToBattlefield(player1, new DarksteelAxe());
            harness.addToBattlefield(player1, new PristineTalisman());

            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            int scimitarIndex = findPermanentIndex(player1, "Leonin Scimitar");

            // The scimitar has its own equip {1} at index 0, and the granted equip {0} at index 1
            harness.activateAbility(player1, scimitarIndex, 1, null, bearsId);
            harness.passBothPriorities(); // resolve equip

            Permanent scimitarPerm = findPermanent(player1, "Leonin Scimitar");
            assertThat(scimitarPerm.getAttachedTo()).isEqualTo(bearsId);
        }

        @Test
        @DisplayName("Metalcraft equip {0} does not apply to opponent's equipment")
        void metalcraftDoesNotApplyToOpponentEquipment() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            // Player 1 has metalcraft
            harness.addToBattlefield(player1, new DarksteelAxe());
            harness.addToBattlefield(player1, new PristineTalisman());
            harness.addToBattlefield(player1, new LeoninScimitar());

            // Opponent has equipment
            harness.addToBattlefield(player2, new LeoninScimitar());

            Permanent opponentScimitar = findPermanent(player2, "Leonin Scimitar");
            GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, opponentScimitar);

            assertThat(bonus.grantedActivatedAbilities()).isEmpty();
        }
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }

    private int findPermanentIndex(Player player, String name) {
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals(name)) return i;
        }
        throw new IllegalStateException(name + " not found");
    }
}
