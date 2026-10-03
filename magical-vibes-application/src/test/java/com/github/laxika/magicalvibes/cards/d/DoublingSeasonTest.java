package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolgariGraveTroll;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.p.PlagueBoiler;
import com.github.laxika.magicalvibes.cards.v.VinelasherKudzu;
import com.github.laxika.magicalvibes.cards.v.VituGhaziTheCityTree;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoublingSeason.class, Forest.class, GolgariGraveTroll.class, HardenedScales.class,
        PlagueBoiler.class, VinelasherKudzu.class,
        VituGhaziTheCityTree.class})
class DoublingSeasonTest extends BaseCardTest {

    @Test
    @DisplayName("Two Doubling Seasons create four times as many tokens")
    void multipleSeasonsMultiplyTokens() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 2, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(4);
    }

    @Test
    @DisplayName("Two Doubling Seasons put four times as many counters on a permanent")
    void multipleSeasonsMultiplyCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubles entry counters but does not multiply counter removal costs")
    void doublesEntryCountersWithoutDoublingRemoval() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setGraveyard(player1, List.of(new VinelasherKudzu(), new VinelasherKudzu()));

        harness.castFromHand(player1, new GolgariGraveTroll(), "{4}{G}");
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Golgari Grave-Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller chooses the order of Doubling Season and Hardened Scales")
    void controllerChoosesCounterReplacementOrder() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Doubles tokens created under its controller's control")
    void doublesTokens() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Does not double tokens created under an opponent's control")
    void doesNotDoubleOpponentsTokens() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player2, new VituGhaziTheCityTree());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Doubles +1/+1 counters put on a permanent its controller controls")
    void doublesPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not double counters put on a permanent controlled by an opponent")
    void doesNotDoubleOpponentsCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent kudzu = harness.addToBattlefieldAndReturn(player2, new VinelasherKudzu());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles noncreature counters put on a permanent its controller controls")
    void doublesNoncreatureCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a plague counter on Plague Boiler");

        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(2);
    }
}
