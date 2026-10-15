package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RabbitBattery.class, JukaiTrainee.class, EnchantedEvening.class})
class RabbitBatteryTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndHaste() {
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        battery.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, battery)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBattery() {
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
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
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        Permanent opponentCreature = addCreatureReady(player2, new JukaiTrainee());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battery.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetItself() {
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, battery.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUnattachWhenAlreadyUnattached() {
        addCreatureReady(player1, new RabbitBattery());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        addReconfigureMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        battery.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battery.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigureCanMoveDirectlyBetweenCreatures() {
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        Permanent first = addCreatureReady(player1, new JukaiTrainee());
        Permanent second = addCreatureReady(player1, new JukaiTrainee());
        battery.setAttachedTo(first.getId());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(battery.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, battery)).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    void reconfigureRetainsOtherCardTypes() {
        Permanent battery = addCreatureReady(player1, new RabbitBattery());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addToBattlefieldAndReturn(player1, new EnchantedEvening()).setTimestamp(0);
        assertThat(gqs.isEnchantment(gd, battery)).isTrue();
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, battery)).isFalse();
        assertThat(gqs.isEnchantment(gd, battery)).isTrue();
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
