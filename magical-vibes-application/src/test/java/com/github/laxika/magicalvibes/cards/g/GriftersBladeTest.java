package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GriftersBlade.class, GrizzlyBears.class})
class GriftersBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Grifter's Blade may attach it to a creature you control")
    void enteringMayAttachToControlledCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent blade = findPermanent(player1, "Grifter's Blade");
        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
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
    @DisplayName("Equip {1} attaches Grifter's Blade to a creature you control")
    void equipAttachesToControlledCreature() {
        harness.setHand(player1, List.of(new GriftersBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Grifter's Blade");
        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }
}
