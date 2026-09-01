package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuxiorGiadasGift.class, ChandraNalaar.class, GrizzlyBears.class})
class LuxiorGiadasGiftTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePlusOneForEachCounterOnIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LuxiorGiadasGift());
        creature.setCounterCount(CounterType.CHARGE, 2);
        creature.setCounterCount(CounterType.AGE, 1);
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void equippedPlaneswalkerBecomesCreatureButIsNotAPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LuxiorGiadasGift());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        equipment.setAttachedTo(planeswalker.getId());

        assertThat(gqs.isCreature(gd, planeswalker)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();
        assertThat(gqs.getEffectivePower(gd, planeswalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, planeswalker)).isEqualTo(3);
    }

    @Test
    void equipPlaneswalkerAbilityAttachesToPlaneswalker() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LuxiorGiadasGift());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(planeswalker.getId());
        assertThat(gqs.isCreature(gd, planeswalker)).isTrue();
    }

    @Test
    void equipPlaneswalkerAbilityCannotTargetCreature() {
        harness.addToBattlefieldAndReturn(player1, new LuxiorGiadasGift());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a planeswalker");
    }

    @Test
    void detachingRestoresPlaneswalkerType() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LuxiorGiadasGift());
        equipment.setAttachedTo(planeswalker.getId());
        assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();

        equipment.setAttachedTo(null);

        assertThat(gqs.isPlaneswalker(gd, planeswalker)).isTrue();
        assertThat(gqs.isCreature(gd, planeswalker)).isFalse();
    }
}
