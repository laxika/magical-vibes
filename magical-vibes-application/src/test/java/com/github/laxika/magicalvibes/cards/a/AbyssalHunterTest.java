package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.cards.v.VigilantMartyr;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbyssalHunter.class, Forest.class, NobleElephant.class, VigilantMartyr.class})
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

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .doesNotContain(target);
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
}
