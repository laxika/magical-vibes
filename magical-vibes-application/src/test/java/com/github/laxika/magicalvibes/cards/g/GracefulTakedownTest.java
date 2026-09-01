package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyArmor;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GracefulTakedown.class, HolyArmor.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class GracefulTakedownTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creatures and one other creature each deal their power as damage")
    void enchantedCreaturesAndOneOtherDealPowerDamage() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent firstEnchanted = addEnchantedBear();
        Permanent secondEnchanted = addEnchantedBear();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareSpell();

        harness.castSorcery(player1, 0,
                List.of(victim.getId(), other.getId(), firstEnchanted.getId(), secondEnchanted.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("A single unenchanted creature can be chosen as the other creature")
    void allowsOneUnenchantedCreature() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareSpell();

        harness.castSorcery(player1, 0, List.of(victim.getId(), other.getId()));
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two unenchanted creatures cannot both be chosen")
    void rejectsTwoUnenchantedCreatures() {
        Permanent firstOther = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(victim.getId(), firstOther.getId(), secondOther.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted creature");
    }

    private Permanent addEnchantedBear() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        aura.setAttachedTo(bear.getId());
        return bear;
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new GracefulTakedown()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
