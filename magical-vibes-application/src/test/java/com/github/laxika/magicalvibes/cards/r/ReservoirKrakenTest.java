package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReservoirKraken.class, GrizzlyBears.class})
class ReservoirKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Declining leaves the Kraken and the opponent's creature untapped")
    void decliningDoesNothing() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(kraken.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("Accepting taps the chosen creature and creates one unblockable Fish")
    void acceptingTapsCreatureAndCreatesFish() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(kraken.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        Permanent fish = findPermanent(player1, "Fish");
        assertThat(gqs.getEffectivePower(gd, fish)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fish)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, fish)).isTrue();
    }

    @Test
    @DisplayName("Choosing among several creatures taps only the selected one")
    void choosesCreatureToTap() {
        harness.addToBattlefield(player1, new ReservoirKraken());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger works during an opponent's combat")
    void triggersDuringOpponentsCombat() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveBeginningOfCombat(player2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(kraken.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Kraken does not trigger")
    void tappedKrakenDoesNotTrigger() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        kraken.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
