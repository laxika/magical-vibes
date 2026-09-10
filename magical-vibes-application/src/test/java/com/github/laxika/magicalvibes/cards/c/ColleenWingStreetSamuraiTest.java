package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColleenWingStreetSamurai.class, GrizzlyBears.class, GiantGrowth.class, Shock.class})
class ColleenWingStreetSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell targeting a creature you control puts a counter on Colleen and scries")
    void ownCreatureTargetTriggersCounterAndScry() {
        Permanent colleen = harness.addToBattlefieldAndReturn(player1, new ColleenWingStreetSamurai());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(colleen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Targeting an opponent's creature does not trigger Colleen")
    void opponentCreatureTargetDoesNotTrigger() {
        Permanent colleen = harness.addToBattlefieldAndReturn(player1, new ColleenWingStreetSamurai());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(colleen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Targeting a player does not trigger Colleen")
    void playerTargetDoesNotTrigger() {
        Permanent colleen = harness.addToBattlefieldAndReturn(player1, new ColleenWingStreetSamurai());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(colleen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
