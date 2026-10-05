package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.cards.t.TerritorialBaloth;
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

@CardUsed({KazuulWarlord.class, KazanduBlademaster.class, TerritorialBaloth.class,
        StoneworkPuma.class, IntoTheRoil.class, Conspiracy.class})
class KazuulWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a counter on each Ally you control")
    void ownAllyEntryMayPutCountersOnEachAlly() {
        harness.castFromHand(player1, new KazuulWarlord(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent warlord = findPermanent(player1, "Kazuul Warlord");
        assertThat(warlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry puts counters on every Ally but not non-Allies")
    void anotherAllyEntryPutsCountersOnEveryAlly() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new TerritorialBaloth());
        harness.castFromHand(player1, new KazuulWarlord(), "{4}{R}");
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Kazuul Warlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter placement may be declined")
    void counterPlacementMayBeDeclined() {
        harness.castFromHand(player1, new KazuulWarlord(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Kazuul Warlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another Ally entry triggers Warlord and affects only its controller's Allies")
    void anotherAllyEnteringTriggersWarlord() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new TerritorialBaloth());

        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(warlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existingAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof StoneworkPuma)
                .hasSize(2)
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(opposingAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Warlord")
    void nonAllyEntryDoesNotTriggerWarlord() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());

        harness.castFromHand(player1, new TerritorialBaloth(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(warlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opposing Ally entry does not trigger Warlord")
    void opposingAllyEntryDoesNotTriggerWarlord() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(warlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Warlord trigger resolves even after Warlord leaves the battlefield")
    void triggerResolvesAfterWarlordLeaves() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());
        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, warlord.getId());
        harness.assertInHand(player1, "Kazuul Warlord");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Stonework Puma")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Warlord's own entry triggers even when its creature type is replaced")
    void ownEntryTriggersWithoutAllySubtype() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        Permanent conspiracy = findPermanent(player1, "Conspiracy");

        harness.castFromHand(player1, new KazuulWarlord(), "{4}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, conspiracy.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Kazuul Warlord")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Warlord triggers independently for an Ally entry")
    void multipleWarlordsEachPutCountersOnAllAllies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());

        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Stonework Puma")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entering Ally can leave before the trigger resolves")
    void enteringAllyLeavingDoesNotPreventCountersOnRemainingAllies() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new KazuulWarlord());
        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        Permanent puma = findPermanent(player1, "Stonework Puma");

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, puma.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Stonework Puma");
        assertThat(warlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(puma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
