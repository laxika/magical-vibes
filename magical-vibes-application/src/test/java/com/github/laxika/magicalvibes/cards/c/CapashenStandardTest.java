package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapashenStandard.class, FountainOfYouth.class, GrizzlyBears.class})
class CapashenStandardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CapashenStandard());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing Capashen Standard draws a card")
    void sacrificingAuraDrawsCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CapashenStandard());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Capashen Standard");
        harness.assertInGraveyard(player1, "Capashen Standard");
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CapashenStandard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CapashenStandard
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Sacrifice removes the bonus immediately and draws for the Aura's controller on resolution")
    void sacrificeOnOpponentsCreaturePaysCostBeforeDrawing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CapashenStandard()));
        CapashenStandard drawnCard = new CapashenStandard();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        int opponentsHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Capashen Standard");
        harness.assertInGraveyard(player1, "Capashen Standard");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentsHandSize);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability without two mana")
    void cannotActivateWithoutTwoMana() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CapashenStandard());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CapashenStandard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
