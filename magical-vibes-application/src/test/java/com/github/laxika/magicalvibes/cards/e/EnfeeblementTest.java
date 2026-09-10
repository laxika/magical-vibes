package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enfeeblement.class, Island.class, TrainedArmodon.class, WindDrake.class})
class EnfeeblementTest extends BaseCardTest {

    // ===== -2/-2 boost =====

    @Test
    @DisplayName("Enchanted creature gets -2/-2")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Enfeeblement());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enfeeblement affects only its enchanted creature")
    void onlyEnchantedCreatureGetsDebuff() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Enfeeblement());
        aura.setAttachedTo(enchantedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving Enfeeblement attaches it and applies -2/-2")
    void resolvingAttachesAndDebuffs() {
        Permanent armodon = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        Enfeeblement enfeeblement = new Enfeeblement();

        harness.setHand(player1, List.of(enfeeblement));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, armodon.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == enfeeblement
                        && p.isAttached()
                        && armodon.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, armodon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, armodon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enfeeblement goes to its owner's graveyard if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent armodon = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        Enfeeblement enfeeblement = new Enfeeblement();

        harness.setHand(player1, List.of(enfeeblement));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, armodon.getId());
        gd.playerBattlefields.get(player2.getId()).remove(armodon);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enfeeblement");
        harness.assertNotOnBattlefield(player1, "Enfeeblement");
    }

    @Test
    @DisplayName("Enfeeblement puts a 2/2 creature into its owner's graveyard")
    void debuffKillsTwoTwoCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Enfeeblement enfeeblement = new Enfeeblement();

        harness.setHand(player1, List.of(enfeeblement));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, drake.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertInGraveyard(player1, "Enfeeblement");
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Enfeeblement")
    void canTargetCreature() {
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Enfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, armodon.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Enfeeblement")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Enfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
