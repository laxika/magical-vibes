package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RabbitBattery.class, GrizzlyBears.class})
class RabbitBatteryTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndHaste() {
        Permanent battery = addReadyBattery(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        battery.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, battery)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBattery() {
        Permanent battery = addReadyBattery(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(battery.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, battery)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(battery.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, battery)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent battery = addReadyBattery(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battery.getAttachedTo()).isNull();
    }

    private Permanent addReadyBattery(Player player) {
        Permanent battery = new Permanent(new RabbitBattery());
        battery.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(battery);
        return battery;
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
