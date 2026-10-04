package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
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

@CardUsed({GhituFirebreathing.class, AshcoatBear.class, ChromaticStar.class})
class GhituFirebreathingTest extends BaseCardTest {

    private Permanent attachTo(Permanent host) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GhituFirebreathing());
        auraPerm.setAttachedTo(host.getId());
        return auraPerm;
    }

    @Test
    @DisplayName("First ability gives enchanted creature +1/+0 until end of turn")
    void abilityBoostsPower() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power boost stacks across activations and wears off at end of turn")
    void boostStacksAndWearsOff() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability returns the Aura to its owner's hand")
    void secondAbilityReturnsAuraToHand() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghitu Firebreathing");
        harness.assertInHand(player1, "Ghitu Firebreathing");
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        GhituFirebreathing auraCard = new GhituFirebreathing();
        harness.setHand(player1, List.of(auraCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == auraCard)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Can cast Ghitu Firebreathing at instant speed during an opponent's turn")
    void canCastAtInstantSpeedAsNonActivePlayer() {
        harness.forceActivePlayer(player2);
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        GhituFirebreathing auraCard = new GhituFirebreathing();
        harness.setHand(player1, List.of(auraCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passPriority(player2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == auraCard)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        harness.setHand(player1, List.of(new GhituFirebreathing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Aura controller can boost an opponent's enchanted creature")
    void boostsOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new AshcoatBear());
        GhituFirebreathing auraCard = new GhituFirebreathing();
        harness.setHand(player1, List.of(auraCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Ghitu Firebreathing");
    }

    @Test
    @DisplayName("Resolved boost remains after the Aura returns to hand")
    void resolvedBoostSurvivesAuraReturning() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        attachTo(bears);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ghitu Firebreathing");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pending boost resolves using the enchanted creature after the Aura returns")
    void pendingBoostSurvivesAuraReturning() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        attachTo(bears);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ghitu Firebreathing");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
