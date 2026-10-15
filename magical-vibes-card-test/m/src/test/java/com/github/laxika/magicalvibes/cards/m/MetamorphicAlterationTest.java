package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetamorphicAlteration.class, GrizzlyBears.class, HillGiant.class, AirElemental.class, Demystify.class})
class MetamorphicAlterationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the Aura prompts the controller to choose a creature")
    void resolvingPromptsCreatureChoice() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Enchanted creature becomes a copy of the chosen creature")
    void enchantedCreatureBecomesCopyOfChosenCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The copy picks up the chosen creature's keywords")
    void copyGainsChosenCreatureKeywords() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elemental.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature reverts when the Aura leaves the battlefield")
    void enchantedCreatureRevertsWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());
        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");

        Permanent aura = findPermanent(player1, "Metamorphic Alteration");
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(bears.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Aura can only enchant a creature")
    void cannotEnchantANonCreature() {
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new MetamorphicAlteration());
        harness.setHand(player1, List.of(new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, otherAura.getId()))
                .hasMessageContaining("can only enchant a creature");
    }
    @Test
    @DisplayName("Self-copy preserves copied values after an earlier Aura leaves")
    void selfCopyPreservesCopiedValues() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new MetamorphicAlteration(), new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());
        Permanent firstAura = findPermanent(player1, "Metamorphic Alteration");
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, firstAura.getId());
        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(findPermanents(player1, "Metamorphic Alteration")).hasSize(1);
    }

    @Test
    @DisplayName("Later changes to the chosen creature do not change the copy")
    void laterChangesToChosenCreatureDoNotChangeCopy() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new MetamorphicAlteration(), new MetamorphicAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elemental.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isTrue();
        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
