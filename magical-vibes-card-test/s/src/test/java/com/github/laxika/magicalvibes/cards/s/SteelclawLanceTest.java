package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelclawLance.class, BenalishKnight.class, GrizzlyBears.class, Unsummon.class})
class SteelclawLanceTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusTwo() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lance.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void knightEquipAttachesToKnightForOneMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(knight.getId());
    }

    @Test
    void knightEquipRejectsNonKnight() {
        harness.addToBattlefield(player1, new SteelclawLance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Knight");
    }

    @Test
    void genericEquipAttachesToNonKnightForThreeMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void movingEquipmentTransfersTheBonusOnlyWhenTheAbilityResolves() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, knight.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, bear.getId());

        assertThat(lance.getAttachedTo()).isEqualTo(knight.getId());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    void bothEquipAbilitiesRejectOpposingKnights() {
        harness.addToBattlefield(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothEquipAbilitiesRejectActivationOutsideTheMainPhase() {
        harness.addToBattlefield(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void genericEquipDoesNotReceiveTheKnightDiscount() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lance.getAttachedTo()).isNull();
    }

    @Test
    void knightEquipRequiresOneMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lance.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void failedKnightEquipLeavesEquipmentOnItsPreviousCreature() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new SteelclawLance());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Unsummon()));

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, knight.getId());
        harness.castAndResolveInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Benalish Knight");
        assertThat(lance.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
