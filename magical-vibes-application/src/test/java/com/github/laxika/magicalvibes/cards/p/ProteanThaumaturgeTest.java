package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProteanThaumaturge.class, GloriousAnthem.class, GrizzlyBears.class, HillGiant.class})
class ProteanThaumaturgeTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control lets it become another creature")
    void enchantmentTriggerCopiesAnotherCreature() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(thaumaturge.getCard().getPower()).isEqualTo(2);
        assertThat(thaumaturge.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copied creature retains the constellation copy ability")
    void copyRetainsConstellationAbility() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent hillGiant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(thaumaturge.getCard().getPower()).isEqualTo(3);
        assertThat(thaumaturge.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The constellation trigger cannot target Protean Thaumaturge itself")
    void triggerCannotTargetItself() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Protean Thaumaturge");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
