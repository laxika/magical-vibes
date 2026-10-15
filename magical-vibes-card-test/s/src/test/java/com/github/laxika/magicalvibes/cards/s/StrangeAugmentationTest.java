package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LupinePrototype;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrangeAugmentation.class, GrizzlyBears.class, Plains.class, Shock.class,
        Millstone.class, FountainOfYouth.class, LupinePrototype.class})
class StrangeAugmentationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1, plus an additional +2/+2 with delirium")
    void boostsEnchantedCreatureWithDelirium() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Delirium checks the Aura controller's graveyard")
    void deliriumUsesAuraControllersGraveyard() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, bears);

        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = addCreatureReady(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new StrangeAugmentation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving the Aura boosts only the enchanted creature")
    void resolvesOntoCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StrangeAugmentation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Strange Augmentation").getAttachedTo())
                .isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Four cards with only three card types do not enable delirium")
    void countsDistinctCardTypesAndLosesDelirium() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, bears);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Shock()
        ));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("An artifact creature contributes both card types to delirium")
    void countsMultipleTypesOnOneCard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, bears);
        harness.setGraveyard(player1, List.of(
                new LupinePrototype(), new Plains(), new Shock()
        ));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    private void addAura(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new StrangeAugmentation());
        aura.setAttachedTo(enchanted.getId());
    }
}
