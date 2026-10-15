package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({KazanduBlademaster.class, GrizzlyBears.class, Conspiracy.class})
class KazanduBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a +1/+1 counter on it")
    void ownAllyEntryMayPutCounterOnIt() {
        harness.castFromHand(player1, new KazanduBlademaster(), "{W}{W}");
        resolveAllTriggers();

        Permanent blademaster = findPermanent(player1, "Kazandu Blademaster");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry triggers both the existing and entering Blademasters")
    void anotherAllyEntryTriggersBothBlademasters() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        harness.castFromHand(player1, new KazanduBlademaster(), "{W}{W}");
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
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void mayBeDeclined() {
        harness.castFromHand(player1, new KazanduBlademaster(), "{W}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Kazandu Blademaster")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger your Blademaster")
    void opponentsAllyDoesNotTrigger() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new KazanduBlademaster(), "{W}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Kazandu Blademaster")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Its own entry still triggers when its creature types are replaced")
    void ownEntryTriggersWithoutAllySubtype() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new KazanduBlademaster(), "{W}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Kazandu Blademaster")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each subsequent Ally entry can add another counter")
    void repeatedAllyEntriesAccumulateCounters() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        harness.setHand(player1, List.of(new KazanduBlademaster(), new KazanduBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        for (int entry = 0; entry < 2; entry++) {
            harness.castCreature(player1, 0);
            for (int trigger = 0; trigger < entry + 2; trigger++) {
                resolveAllTriggers();
                harness.handleMayAbilityChosen(player1, true);
            }
            resolveAllTriggers();
            assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(entry + 1);
        }

        List<Permanent> blademasters = findPermanents(player1, "Kazandu Blademaster");
        assertThat(blademasters.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blademasters.get(2).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
