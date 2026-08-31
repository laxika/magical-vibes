package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBoulderReadyToRumble.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class TheBoulderReadyToRumbleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking earthbends a land with one counter per controlled creature with power 4 or greater")
    void attackEarthbendsForControlledHighPowerCreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent boulder = addCreatureReady(player1, new TheBoulderReadyToRumble());
        addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new SerraAngel());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(boulder)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(land.getId()).doesNotContain(opposingLand.getId());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }
}
