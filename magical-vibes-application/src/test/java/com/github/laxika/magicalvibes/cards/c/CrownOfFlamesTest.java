package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
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

@CardUsed({CrownOfFlames.class, TrainedArmodon.class, CursedScroll.class})
class CrownOfFlamesTest extends BaseCardTest {

    private Permanent attachTo(Permanent host) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CrownOfFlames());
        auraPerm.setAttachedTo(host.getId());
        return auraPerm;
    }

    @Test
    @DisplayName("First ability gives enchanted creature +1/+0 until end of turn")
    void abilityBoostsPower() {
        Permanent bears = addCreatureReady(player1, new TrainedArmodon());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power boost stacks across activations and wears off at end of turn")
    void boostStacksAndWearsOff() {
        Permanent bears = addCreatureReady(player1, new TrainedArmodon());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Second ability returns the Aura to its owner's hand")
    void secondAbilityReturnsAuraToHand() {
        Permanent bears = addCreatureReady(player1, new TrainedArmodon());
        Permanent aura = attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> aura.getId().equals(p.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(aura.getCard());
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player1, new TrainedArmodon());
        CrownOfFlames auraCard = new CrownOfFlames();
        harness.setHand(player1, List.of(auraCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == auraCard)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        harness.setHand(player1, List.of(new CrownOfFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
