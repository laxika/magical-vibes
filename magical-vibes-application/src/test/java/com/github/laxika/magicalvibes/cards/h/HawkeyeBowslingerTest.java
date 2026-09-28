package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeBowslinger.class, GrizzlyBears.class, Shock.class})
class HawkeyeBowslingerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets a creature puts a counter on Hawkeye and scries 1")
    void creatureTargetTriggersCounterAndScry() {
        harness.addToBattlefield(player1, new HawkeyeBowslinger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent hawkeye = findPermanent(player1, "Hawkeye, Bowslinger");
        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Hawkeye, Bowslinger"));

        harness.passBothPriorities();

        assertThat(hawkeye.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Hawkeye")
    void playerTargetDoesNotTrigger() {
        harness.addToBattlefield(player1, new HawkeyeBowslinger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Hawkeye, Bowslinger"));
    }
}
