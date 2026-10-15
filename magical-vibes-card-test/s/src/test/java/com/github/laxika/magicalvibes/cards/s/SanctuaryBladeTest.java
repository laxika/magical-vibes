package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctuaryBlade.class, GrizzlyBears.class})
class SanctuaryBladeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SanctuaryBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void attachmentChoosesColorAndGrantsProtection() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SanctuaryBlade());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(blade.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    void protectionEndsWhenEquipmentBecomesUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SanctuaryBlade());
        blade.setAttachedTo(creature.getId());
        blade.setChosenColor(CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();

        blade.setAttachedTo(null);

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
    }

    @Test
    void equippingSameCreatureDoesNotChooseAnotherColor() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SanctuaryBlade());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blade.setAttachedTo(creature.getId());
        blade.setChosenColor(CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(blade.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    void movingEquipmentChoosesNewColorDuringEquipResolution() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SanctuaryBlade());
        Permanent oldCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player1, new GrizzlyBears());
        blade.setAttachedTo(oldCreature.getId());
        blade.setChosenColor(CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(blade.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, oldCreature, CardColor.RED)).isFalse();
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, newCreature, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, newCreature, CardColor.RED)).isFalse();
    }

    @Test
    void attachedWithoutChosenColorGrantsNoProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new SanctuaryBlade());
        blade.setAttachedTo(creature.getId());

        for (CardColor color : java.util.List.of(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN)) {
            assertThat(gqs.hasProtectionFrom(gd, creature, color)).isFalse();
        }
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }
}
