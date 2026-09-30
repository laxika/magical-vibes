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
        resolveAllTriggers();

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
}
