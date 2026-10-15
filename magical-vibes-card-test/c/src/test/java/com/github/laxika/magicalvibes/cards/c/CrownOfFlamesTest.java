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

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

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

    @Test
    @DisplayName("Pump resolves using the enchanted creature after the Aura returns to hand")
    void pumpResolvesAfterAuraReturnsToHand() {
        Permanent creature = addCreatureReady(player1, new TrainedArmodon());
        Permanent aura = attachTo(creature);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(aura.getCard());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A resolved pump persists after the Aura returns to hand")
    void resolvedPumpPersistsWithoutAura() {
        harness.setHand(player1, java.util.List.of());
        Permanent creature = addCreatureReady(player1, new TrainedArmodon());
        attachTo(creature);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crown of Flames");
        harness.assertNotOnBattlefield(player1, "Crown of Flames");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Aura controller can enchant and pump an opponent's creature and return the Aura")
    void canEnchantAndPumpOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new CrownOfFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crown of Flames");
        harness.assertNotInHand(player2, "Crown of Flames");
        harness.assertOnBattlefield(player2, "Trained Armodon");
    }
}
