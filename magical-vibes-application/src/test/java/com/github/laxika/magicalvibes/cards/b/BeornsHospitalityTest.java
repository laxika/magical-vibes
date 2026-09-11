package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeornsHospitality.class, Forest.class, GrizzlyBears.class})
class BeornsHospitalityTest extends BaseCardTest {

    @Test
    void landfallPutsACounterOnTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new BeornsHospitality());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activationMakesItABearWithPowerAndToughnessEqualToControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent hospitality = harness.addToBattlefieldAndReturn(player1, new BeornsHospitality());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hospitality)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hospitality)).contains(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, hospitality)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hospitality)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, hospitality)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hospitality)).isEqualTo(3);
    }
}
