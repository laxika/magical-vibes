package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HadaFreeblade.class, GrizzlyBears.class, Conspiracy.class})
class HadaFreebladeTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a +1/+1 counter on it")
    void ownAllyEntryMayPutCounterOnIt() {
        harness.castFromHand(player1, new HadaFreeblade(), "{W}");
        resolveAllTriggers();

        Permanent freeblade = findPermanent(player1, "Hada Freeblade");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry triggers both the existing and entering Freeblades")
    void anotherAllyEntryTriggersBothFreeblades() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HadaFreeblade());
        harness.castFromHand(player1, new HadaFreeblade(), "{W}");
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent freeblade = harness.addToBattlefieldAndReturn(player1, new HadaFreeblade());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void mayBeDeclined() {
        harness.castFromHand(player1, new HadaFreeblade(), "{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Hada Freeblade")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger the existing Freeblade")
    void opponentsAllyEntryDoesNotTrigger() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HadaFreeblade());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new HadaFreeblade(), "{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Hada Freeblade")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The choices for two Freeblades are independent")
    void mayChoicesAreIndependent() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HadaFreeblade());
        harness.castFromHand(player1, new HadaFreeblade(), "{W}");
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                + entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Its own entry still triggers when Conspiracy removes the Ally type")
    void ownEntryTriggersWithoutAllyType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new HadaFreeblade(), "{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Hada Freeblade")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
