package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AboshanCephalidEmperorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Octopus taps target permanent")
    void tappingOctopusTapsTargetPermanent() {
        Permanent aboshan = addReady(player1, new AboshanCephalidEmperor());
        Permanent target = addReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, aboshan), null, target.getId());
        assertThat(aboshan.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The first ability requires an untapped Octopus")
    void firstAbilityRequiresUntappedOctopus() {
        Permanent aboshan = addReady(player1, new AboshanCephalidEmperor());
        aboshan.tap();
        Permanent target = addReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, aboshan), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The three-blue ability taps every creature without flying")
    void threeBlueAbilityTapsCreaturesWithoutFlying() {
        Permanent aboshan = addReady(player1, new AboshanCephalidEmperor());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opposingBears = addReady(player2, new GrizzlyBears());
        Permanent flyer = addReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, aboshan), 1, null, null);
        harness.passBothPriorities();

        assertThat(aboshan.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(opposingBears.isTapped()).isTrue();
        assertThat(flyer.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
