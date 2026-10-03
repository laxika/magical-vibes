package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.l.Launch;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Crosswinds.class, CradleGuard.class, Zephid.class, Launch.class, Disenchant.class})
class CrosswindsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crosswinds debuffs existing flyers until it leaves the battlefield")
    void resolvingAndRemovingCrosswindsUpdatesPower() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());
        harness.castFromHand(player1, new Crosswinds(), "{1}{G}");

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Crosswinds").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crosswinds");
        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("The debuff starts and stops when a creature gains and loses flying")
    void followsGrantedFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CradleGuard());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.setHand(player1, List.of(new Launch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Launch").getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Launch");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures with flying get -2/-0")
    void debuffsCreaturesWithFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void doesNotDebuffCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CradleGuard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The effect applies to flying creatures controlled by either player")
    void affectsOwnFlyingCreature() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Crosswinds effects stack")
    void effectsStack() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Stacked effects can reduce flying creatures to negative power")
    void effectsCanReducePowerBelowZero() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }
}
