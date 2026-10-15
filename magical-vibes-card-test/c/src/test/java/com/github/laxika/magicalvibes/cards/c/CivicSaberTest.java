package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CivicSaber.class, DrudgeBeetle.class, CentaurHealer.class, Ornithopter.class})
class CivicSaberTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Civic Saber to a creature you control")
    void equipAttachesToCreature() {
        Permanent saber = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(saber.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each color")
    void boostScalesWithColors() {
        Permanent creature = addCreatureReady(player1, new CentaurHealer());
        Permanent saber = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        saber.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Civic Saber gives no bonus to a colorless creature")
    void colorlessCreatureGetsNoBonus() {
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        Permanent saber = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        saber.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void reequippingMovesTheBonusToTheNewCreature() {
        Permanent saber = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        Permanent first = addCreatureReady(player1, new CentaurHealer());
        Permanent second = addCreatureReady(player1, new DrudgeBeetle());
        saber.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(saber.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void multipleSabersEachCountTheEquippedCreaturesColors() {
        Permanent creature = addCreatureReady(player1, new CentaurHealer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent saber = harness.addToBattlefieldAndReturn(player1, new CivicSaber());
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(saber.getAttachedTo()).isNull();
    }
}