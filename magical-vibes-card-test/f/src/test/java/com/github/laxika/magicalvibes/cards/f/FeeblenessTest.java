package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.ScrybRanger;
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

@CardUsed({Feebleness.class, BenalishCavalry.class, PrismaticLens.class, ScrybRanger.class})
class FeeblenessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Feebleness attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        harness.setHand(player1, List.of(new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Feebleness && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Flash allows Feebleness to be cast during an opponent's turn")
    void canBeCastDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature gets -2/-1")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Feebleness());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Feebleness is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Feebleness());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Feebleness puts a 1/1 creature into the graveyard through its toughness reduction")
    void killsCreatureWithOneToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScrybRanger());
        harness.setHand(player1, List.of(new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scryb Ranger");
        harness.assertInGraveyard(player1, "Scryb Ranger");
    }

    @Test
    @DisplayName("Feebleness fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        harness.setHand(player1, List.of(new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Feebleness");
        harness.assertNotOnBattlefield(player1, "Feebleness");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Feebleness")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Feebleness affects only the enchanted opponent's creature")
    void debuffsOpponentsCreatureOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Feebleness Auras reduce toughness to zero and both go to their owner's graveyard")
    void stackingAurasKillCreatureAndBothAurasGoToGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new Feebleness(), new Feebleness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
        harness.assertNotOnBattlefield(player1, "Feebleness");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Feebleness)
                .hasSize(2);
    }
}
