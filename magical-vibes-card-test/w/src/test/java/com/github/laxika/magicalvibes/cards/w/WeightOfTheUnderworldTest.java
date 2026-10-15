package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeightOfTheUnderworld.class, HillGiant.class, GrizzlyBears.class, FountainOfYouth.class})
class WeightOfTheUnderworldTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Weight of the Underworld attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new WeightOfTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Weight of the Underworld")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets -3/-2")
    void enchantedCreatureGetsDebuff() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeightOfTheUnderworld());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when the Aura is removed")
    void effectsStopWhenRemoved() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WeightOfTheUnderworld());
        aura.setAttachedTo(giant.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Weight of the Underworld kills a creature with 2 or less toughness")
    void killsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WeightOfTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Negative power is allowed and only the enchanted creature is weakened")
    void allowsNegativePowerWithoutAffectingOtherCreatures() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        enchanted.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new WeightOfTheUnderworld(), new WeightOfTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchanted);
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Both Auras go to their owner's graveyard when their combined debuff kills the creature")
    void cumulativeDebuffsKillCreatureAndPutAurasInGraveyard() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new WeightOfTheUnderworld(), new WeightOfTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Weight of the Underworld");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof WeightOfTheUnderworld)
                .hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new WeightOfTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
