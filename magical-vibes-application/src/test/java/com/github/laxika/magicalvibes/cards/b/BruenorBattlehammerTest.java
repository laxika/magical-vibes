package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruenorBattlehammer.class, GrizzlyBears.class, SwiftfootBoots.class})
class BruenorBattlehammerTest extends BaseCardTest {

    @Test
    void countsEquipmentAttachedToEachCreatureIndividually() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent creatureWithTwoEquipment = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creatureWithoutEquipment = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        firstEquipment.setAttachedTo(creatureWithTwoEquipment.getId());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        secondEquipment.setAttachedTo(creatureWithTwoEquipment.getId());

        assertThat(gqs.getEffectivePower(gd, creatureWithTwoEquipment)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creatureWithTwoEquipment)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creatureWithoutEquipment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creatureWithoutEquipment)).isEqualTo(2);
    }

    @Test
    void firstEquipActivationIsFreeAndLaterActivationPaysNormally() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
