package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriftersBlade.class, BorosRecruit.class})
class GriftersBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Grifter's Blade may attach it to a creature you control")
    void enteringMayAttachToControlledCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent blade = findPermanent(player1, "Grifter's Blade");
        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("With no legal creature, Grifter's Blade enters unattached")
    void entryAttachmentHasNoChoiceWithoutLegalCreature() {
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Grifter's Blade");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Entering Grifter's Blade cannot attach it to an opponent's creature")
    void entryAttachmentCannotChooseOpponentsCreature() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Grifter's Blade");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {1} attaches Grifter's Blade to a creature you control")
    void equipAttachesToControlledCreature() {
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Grifter's Blade");
        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip {1} moves Grifter's Blade's bonus to the new creature")
    void equipMovesBonusToNewCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new GriftersBlade());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {1} cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new GriftersBlade());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows Grifter's Blade to be cast during an opponent's turn")
    void flashAllowsCastingDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grifter's Blade");
    }
}
