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

    @Test
    @DisplayName("An opponent's spell targeting Hawkeye does not trigger its ability")
    void opponentCreatureTargetDoesNotTrigger() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeBowslinger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hawkeye.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(hawkeye.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Targeting Hawkeye triggers before the spell and allows scrying to the bottom")
    void selfTargetTriggersAndCanBottomTopCard() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeBowslinger());
        Shock top = new Shock();
        HawkeyeBowslinger next = new HawkeyeBowslinger();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hawkeye.getId());
        harness.passBothPriorities();

        assertThat(hawkeye.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hawkeye, Bowslinger");
    }

    @Test
    @DisplayName("Hawkeye still receives its counter when its controller's library is empty")
    void emptyLibraryDoesNotPreventCounter() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeBowslinger());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hawkeye.getId());
        harness.passBothPriorities();

        assertThat(hawkeye.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability still scries if Hawkeye leaves the battlefield before it resolves")
    void sourceLeavingDoesNotPreventScry() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeBowslinger());
        Shock top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hawkeye.getId());
        harness.castAndResolveInstant(player2, 0, hawkeye.getId());
        harness.castAndResolveInstant(player2, 0, hawkeye.getId());
        harness.assertNotOnBattlefield(player1, "Hawkeye, Bowslinger");
        harness.assertInGraveyard(player1, "Hawkeye, Bowslinger");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
