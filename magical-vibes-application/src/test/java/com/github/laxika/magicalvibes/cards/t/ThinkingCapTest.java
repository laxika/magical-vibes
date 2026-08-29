package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessDetective;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThinkingCap.class, GrizzlyBears.class, RecklessDetective.class})
class ThinkingCapTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cap = addReadyCap();
        cap.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip Detective {1} attaches to a Detective")
    void equipDetectiveAttachesToDetective() {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, detective.getId());
        harness.passBothPriorities();

        assertThat(cap.getAttachedTo()).isEqualTo(detective.getId());
    }

    @Test
    @DisplayName("Equip Detective {1} cannot target a non-Detective")
    void equipDetectiveCannotTargetNonDetective() {
        Permanent cap = addReadyCap();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cap.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} can attach to a non-Detective")
    void genericEquipAttachesToNonDetective() {
        Permanent cap = addReadyCap();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cap.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReadyCap() {
        Permanent cap = harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
        cap.setSummoningSick(false);
        return cap;
    }
}
