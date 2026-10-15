package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodchiefAscension.class, Island.class, Shock.class, TomeScour.class,
        LeylineOfSanctity.class, Naturalize.class})
class BloodchiefAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Two life lost by an opponent at an end step offers a quest counter")
    void twoLifeLostOffersQuestCounter() {
        Permanent ascension = addAscension();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Three quest counters make an opponent's noncreature card loss trigger drain")
    void opponentCardGraveyardTriggersDrain() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.setLibrary(player2, List.of(new Island()));

        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
    }

    @Test
    @DisplayName("Declining the opponent graveyard trigger causes no life change")
    void decliningOpponentCardGraveyardTriggerDoesNothing() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.setLibrary(player2, List.of(new Island()));

        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life);
    }

    @Test
    @DisplayName("Fewer than three quest counters do not create the opponent graveyard trigger")
    void fewerThanThreeQuestCountersDoNotTrigger() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 2);
        harness.setLibrary(player2, List.of(new Island()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentWithHexproofStillLosesLifeWhenTheirSpellEntersGraveyard() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new TomeScour()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void removingQuestCountersBeforeResolutionStopsDrain() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        ascension.setCounterCount(CounterType.QUEST, 2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void destroyingAscensionAfterTriggerUsesLastKnownQuestCounters() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new TomeScour(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, ascension.getId());
        harness.assertInGraveyard(player1, "Bloodchief Ascension");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void controllersOwnGraveyardDoesNotTriggerDrain() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void noLifeLostDoesNotTriggerAtEndStep() {
        Permanent ascension = addAscension();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void lifeLostBeforeAscensionEntersStillOffersQuestCounter() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        Permanent ascension = addAscension();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void eachMilledCardTriggersSeparately() {
        Permanent ascension = addAscension();
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void questCounterMayBeDeclinedOnControllersEndStep() {
        Permanent ascension = addAscension();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    private Permanent addAscension() {
        return harness.addToBattlefieldAndReturn(player1, new BloodchiefAscension());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        if (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
