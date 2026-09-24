package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UncivilUnrest.class, GrizzlyBears.class, ProdigalSorcerer.class})
class UncivilUnrestTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creatures you control get riot")
    void grantsRiotToNontokenCreatures() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles damage from a controlled creature with a +1/+1 counter")
    void doublesDamageFromCounteredCreature() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a controlled creature without a +1/+1 counter")
    void doesNotDoubleDamageFromUncounteredCreature() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
