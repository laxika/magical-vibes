package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AboshanCephalidEmperor.class, AvenFlock.class, Forest.class, Werebear.class})
class AboshanCephalidEmperorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Octopus taps target permanent")
    void tappingOctopusTapsTargetPermanent() {
        Permanent aboshan = addCreatureReady(player1, new AboshanCephalidEmperor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, battlefieldIndex(player1, aboshan), null, target.getId());
        assertThat(aboshan.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The first ability requires an untapped Octopus")
    void firstAbilityRequiresUntappedOctopus() {
        Permanent aboshan = addCreatureReady(player1, new AboshanCephalidEmperor());
        addCreatureReady(player1, new Werebear());
        aboshan.tap();
        Permanent target = addCreatureReady(player2, new Werebear());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, aboshan), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first ability cannot tap an Octopus with summoning sickness")
    void firstAbilityCannotUseSummoningSickOctopus() {
        Permanent aboshan = harness.addToBattlefieldAndReturn(player1, new AboshanCephalidEmperor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, aboshan), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(aboshan.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The three-blue ability taps every creature without flying")
    void threeBlueAbilityTapsCreaturesWithoutFlying() {
        Permanent aboshan = addCreatureReady(player1, new AboshanCephalidEmperor());
        Permanent groundCreature = addCreatureReady(player1, new Werebear());
        Permanent opposingGroundCreature = addCreatureReady(player2, new Werebear());
        Permanent flyer = addCreatureReady(player2, new AvenFlock());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, aboshan), 1, null, null);
        harness.passBothPriorities();

        assertThat(aboshan.isTapped()).isTrue();
        assertThat(groundCreature.isTapped()).isTrue();
        assertThat(opposingGroundCreature.isTapped()).isTrue();
        assertThat(flyer.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The second ability requires three blue mana")
    void threeBlueAbilityRequiresThreeBlueMana() {
        Permanent aboshan = addCreatureReady(player1, new AboshanCephalidEmperor());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, aboshan), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(aboshan.isTapped()).isFalse();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
