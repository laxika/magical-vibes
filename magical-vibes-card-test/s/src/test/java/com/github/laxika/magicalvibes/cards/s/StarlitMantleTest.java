package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarlitMantle.class, RiptideTurtle.class, AltarOfThePantheon.class})
class StarlitMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and gains hexproof until end of turn")
    void enchantedCreatureGetsBoostAndTemporaryHexproof() {
        Permanent bears = castAuraOnCreature();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a creature an opponent controls")
    void cannotEnchantOpponentCreature() {
        harness.addToBattlefield(player1, new RiptideTurtle());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RiptideTurtle());
        harness.setHand(player1, List.of(new StarlitMantle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AltarOfThePantheon());
        harness.setHand(player1, List.of(new StarlitMantle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent creature = castAuraOnCreature();

        harness.assertOnBattlefield(player1, "Starlit Mantle");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The enter trigger grants hexproof even if the creature gains shroud in response")
    void enterTriggerDoesNotTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RiptideTurtle());
        harness.setHand(player1, List.of(new StarlitMantle()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        creature.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        harness.assertOnBattlefield(player1, "Starlit Mantle");
    }

    private Permanent castAuraOnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RiptideTurtle());
        harness.setHand(player1, List.of(new StarlitMantle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return bears;
    }
}
