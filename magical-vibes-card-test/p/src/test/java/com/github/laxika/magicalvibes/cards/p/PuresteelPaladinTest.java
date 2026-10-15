package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.SwordOfWarAndPeace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PuresteelPaladin.class, SwordOfWarAndPeace.class, PristineTalisman.class})
class PuresteelPaladinTest extends BaseCardTest {

    @Nested
    @CardUsed({PuresteelPaladin.class, SwordOfWarAndPeace.class, PristineTalisman.class})
    @DisplayName("Equipment entering draw trigger")
    class EquipmentEnteringDrawTrigger {

        @Test
        @DisplayName("Casting equipment triggers may-draw when Paladin is on battlefield")
        void castingEquipmentTriggersMayDraw() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new SwordOfWarAndPeace()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            resolveAllTriggers();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may-draw draws a card")
        void acceptingMayDrawDrawsCard() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new SwordOfWarAndPeace()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            resolveAllTriggers();

            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();

            int deckSizeAfter = gd.playerDecks.get(player1.getId()).size();
            assertThat(deckSizeAfter).isEqualTo(deckSizeBefore - 1);
        }

        @Test
        @DisplayName("Declining may-draw does not draw a card")
        void decliningMayDrawDoesNotDraw() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player1, List.of(new SwordOfWarAndPeace()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            resolveAllTriggers();
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
            resolveAllTriggers();

            // No may ability prompt since PristineTalisman is not equipment
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        }

        @Test
        @DisplayName("Trigger does not fire for opponent's equipment entering")
        void doesNotTriggerForOpponentEquipment() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            harness.setHand(player2, List.of(new SwordOfWarAndPeace()));
            harness.addMana(player2, ManaColor.COLORLESS, 3);

            harness.forceActivePlayer(player2);
            harness.castArtifact(player2, 0);
            resolveAllTriggers();

            // Paladin only triggers for equipment under your control
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        }
    }

    @Nested
    @CardUsed({PuresteelPaladin.class, SwordOfWarAndPeace.class, PristineTalisman.class})
    @DisplayName("Metalcraft equip {0}")
    class MetalcraftEquipZero {

        @Test
        @DisplayName("Equipment gains equip {0} when metalcraft is active")
        void equipmentGainsEquipZeroWithMetalcraft() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            SwordOfWarAndPeace sword = new SwordOfWarAndPeace();
            harness.addToBattlefield(player1, sword);
            // Need 3 artifacts for metalcraft — add two more
            harness.addToBattlefield(player1, new PristineTalisman());
            harness.addToBattlefield(player1, new PristineTalisman());

            Permanent swordPermanent = findPermanent(player1, "Sword of War and Peace");
            UUID targetId = harness.getPermanentId(player1, "Puresteel Paladin");
            harness.activateAbility(player1, findPermanentIndex(player1, "Sword of War and Peace"), 1, null, targetId);
            harness.passBothPriorities();
            assertThat(swordPermanent.getAttachedTo()).isEqualTo(targetId);
        }

        @Test
        @DisplayName("Equipment does NOT gain equip {0} without metalcraft")
        void equipmentDoesNotGainEquipZeroWithoutMetalcraft() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            SwordOfWarAndPeace sword = new SwordOfWarAndPeace();
            harness.addToBattlefield(player1, sword);
            // Only 1 artifact (sword), metalcraft not met

            Permanent swordPermanent = findPermanent(player1, "Sword of War and Peace");
            UUID targetId = harness.getPermanentId(player1, "Puresteel Paladin");
            assertThatThrownBy(() -> harness.activateAbility(player1,
                    findPermanentIndex(player1, "Sword of War and Peace"), 1, null, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid ability index");
            assertThat(swordPermanent.getAttachedTo()).isNull();
        }

        @Test
        @DisplayName("Equip {0} can be activated to attach equipment to creature")
        void equipZeroCanAttachToCreature() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            SwordOfWarAndPeace sword = new SwordOfWarAndPeace();
            harness.addToBattlefield(player1, sword);
            harness.addToBattlefield(player1, new PristineTalisman());
            harness.addToBattlefield(player1, new PristineTalisman());

            UUID paladinId = harness.getPermanentId(player1, "Puresteel Paladin");
            int swordIndex = findPermanentIndex(player1, "Sword of War and Peace");

            // The sword has its own equip {2} at index 0, and the granted equip {0} at index 1
            harness.activateAbility(player1, swordIndex, 1, null, paladinId);
            harness.passBothPriorities(); // resolve equip

            Permanent swordPermanent = findPermanent(player1, "Sword of War and Peace");
            assertThat(swordPermanent.getAttachedTo()).isEqualTo(paladinId);
        }

        @Test
        @DisplayName("Metalcraft equip {0} does not apply to opponent's equipment")
        void metalcraftDoesNotApplyToOpponentEquipment() {
            harness.addToBattlefield(player1, new PuresteelPaladin());
            // Player 1 has metalcraft
            harness.addToBattlefield(player1, new PristineTalisman());
            harness.addToBattlefield(player1, new PristineTalisman());
            harness.addToBattlefield(player1, new SwordOfWarAndPeace());

            // Opponent has equipment
            harness.addToBattlefield(player2, new SwordOfWarAndPeace());

            Permanent opponentSword = findPermanent(player2, "Sword of War and Peace");
            harness.addToBattlefield(player2, new PuresteelPaladin());
            harness.forceActivePlayer(player2);
            UUID targetId = harness.getPermanentId(player2, "Puresteel Paladin");
            assertThatThrownBy(() -> harness.activateAbility(player2,
                    findPermanentIndex(player2, "Sword of War and Peace"), 1, null, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid ability index");
            assertThat(opponentSword.getAttachedTo()).isNull();
        }
    }

    @Test
    void equipmentEnteringWithoutBeingCastTriggersDraw() {
        harness.addToBattlefield(player1, new PuresteelPaladin());
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.enterBattlefieldAndReturn(player1, new SwordOfWarAndPeace());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void losingMetalcraftRemovesFreeEquipButKeepsOriginalEquip() {
        harness.addToBattlefield(player1, new PuresteelPaladin());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfWarAndPeace());
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.addToBattlefield(player1, new PristineTalisman());
        UUID targetId = harness.getPermanentId(player1, "Puresteel Paladin");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, talisman));

        assertThatThrownBy(() -> harness.activateAbility(player1,
                findPermanentIndex(player1, "Sword of War and Peace"), 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid ability index");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, findPermanentIndex(player1, "Sword of War and Peace"), 0, null, targetId);
        harness.passBothPriorities();
        assertThat(sword.getAttachedTo()).isEqualTo(targetId);
    }

    @Test
    void freeEquipAlreadyOnStackResolvesAfterMetalcraftIsLost() {
        harness.addToBattlefield(player1, new PuresteelPaladin());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfWarAndPeace());
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.addToBattlefield(player1, new PristineTalisman());
        UUID targetId = harness.getPermanentId(player1, "Puresteel Paladin");
        harness.activateAbility(player1, findPermanentIndex(player1, "Sword of War and Peace"), 1, null, targetId);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, talisman));
        harness.passBothPriorities();
        assertThat(sword.getAttachedTo()).isEqualTo(targetId);
    }

    @Test
    void drawTriggerSurvivesPaladinLeavingBattlefield() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PuresteelPaladin());
        harness.setHand(player1, List.of(new SwordOfWarAndPeace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, paladin));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void twoArtifactsAndOpponentsArtifactDoNotEnableMetalcraft() {
        harness.addToBattlefield(player1, new PuresteelPaladin());
        harness.addToBattlefield(player1, new SwordOfWarAndPeace());
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.addToBattlefield(player2, new PristineTalisman());
        UUID targetId = harness.getPermanentId(player1, "Puresteel Paladin");
        assertThatThrownBy(() -> harness.activateAbility(player1,
                findPermanentIndex(player1, "Sword of War and Peace"), 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid ability index");
    }

    private int findPermanentIndex(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, name));
    }
}
