package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.v.VigilantMartyr;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbyssalHunter.class, Forest.class, GiantGrowth.class, NobleElephant.class, Unsummon.class, VigilantMartyr.class})
class AbyssalHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving taps target creature and deals damage equal to its power")
    void resolvingTapsAndDamagesTarget() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys a target with 1 toughness via lethal power damage")
    void lethalDamageDestroysTarget() {
        addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new VigilantMartyr());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vigilant Martyr");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new AbyssalHunter());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Deals damage even when the target is already tapped")
    void damagesAlreadyTappedTarget() {
        addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        target.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player1, new NobleElephant());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Abyssal Hunter has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Damage uses the Hunter's power at resolution")
    void usesPowerAtResolution() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player1, 0, hunter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Noble Elephant");
    }

    @Test
    @DisplayName("The ability still taps and damages its target after the Hunter leaves")
    void resolvesAfterSourceLeaves() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player2, 0, hunter.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Abyssal Hunter");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An ability with a departed target does not tap or damage that card")
    void doesNotResolveForDepartedTarget() {
        addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Noble Elephant");
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Hunter can target itself and deals lethal damage to itself")
    void canTargetItself() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, hunter.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Abyssal Hunter");
        harness.assertInGraveyard(player1, "Abyssal Hunter");
    }


    @Test
    @DisplayName("A departed Hunter uses its boosted last-known power")
    void usesLastKnownBoostedPower() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, hunter.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player2, 0, hunter.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Abyssal Hunter");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Noble Elephant");
    }

    @Test
    @DisplayName("A tapped Hunter cannot activate its tap ability")
    void cannotActivateWhileTapped() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent target = addCreatureReady(player2, new NobleElephant());
        hunter.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

}
