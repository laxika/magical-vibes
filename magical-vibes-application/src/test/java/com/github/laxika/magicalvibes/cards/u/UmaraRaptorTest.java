package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaraRaptor.class, KrakenHatchling.class, IntoTheRoil.class})
class UmaraRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a +1/+1 counter on it")
    void ownAllyEntryMayPutCounterOnIt() {
        harness.castFromHand(player1, new UmaraRaptor(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Umara Raptor");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry triggers both the existing and entering Raptors")
    void anotherAllyEntryTriggersBothRaptors() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new UmaraRaptor());
        harness.castFromHand(player1, new UmaraRaptor(), "{2}{U}");
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
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new UmaraRaptor());
        harness.castFromHand(player1, new KrakenHatchling(), "{U}");
        harness.passBothPriorities();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void mayBeDeclined() {
        harness.castFromHand(player1, new UmaraRaptor(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Umara Raptor")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger a friendly Raptor")
    void opponentsAllyDoesNotTrigger() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new UmaraRaptor());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new UmaraRaptor(), "{2}{U}");
        harness.passBothPriorities();
        Permanent opposing = findPermanent(player2, "Umara Raptor");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(friendly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A counter ability cannot affect its source after it leaves the battlefield")
    void sourceLeavesBeforeCounterAbilityResolves() {
        harness.castFromHand(player1, new UmaraRaptor(), "{2}{U}");
        harness.passBothPriorities();
        Permanent raptor = findPermanent(player1, "Umara Raptor");
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, raptor.getId());

        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Umara Raptor");
        harness.assertInHand(player1, "Umara Raptor");
        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
