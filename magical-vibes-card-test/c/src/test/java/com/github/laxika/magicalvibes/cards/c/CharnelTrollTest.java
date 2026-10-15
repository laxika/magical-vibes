package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KraulHarpooner;
import com.github.laxika.magicalvibes.cards.g.GolgariGuildgate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharnelTroll.class, KraulHarpooner.class, GolgariGuildgate.class, AssaultSuit.class})
class CharnelTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card from the graveyard puts a +1/+1 counter on Charnel Troll")
    void exilingCreatureFromGraveyardAddsCounter() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new GolgariGuildgate(), new KraulHarpooner()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Golgari Guildgate");
        harness.assertNotInGraveyard(player1, "Kraul Harpooner");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Kraul Harpooner");
    }

    @Test
    @DisplayName("The upkeep trigger chooses among all creature cards in the graveyard")
    void choosesCreatureCardFromGraveyard() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new KraulHarpooner(), new KraulHarpooner()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Kraul Harpooner");
    }

    @Test
    @DisplayName("Without a creature card in the graveyard the upkeep trigger sacrifices Charnel Troll")
    void sacrificesWithoutCreatureCard() {
        harness.addToBattlefield(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new GolgariGuildgate()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Charnel Troll");
        harness.assertInGraveyard(player1, "Charnel Troll");
        harness.assertInGraveyard(player1, "Golgari Guildgate");
    }

    @Test
    @DisplayName("Paying the activated ability cost with a creature card puts a +1/+1 counter on Charnel Troll")
    void activatedAbilityDiscardsCreatureAndAddsCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setHand(player1, List.of(new KraulHarpooner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Kraul Harpooner");
    }

    @Test
    @DisplayName("The activated ability cannot discard a noncreature card")
    void activatedAbilityRequiresCreatureCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new CharnelTroll());
        harness.setHand(player1, List.of(new GolgariGuildgate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's upkeep does not trigger Charnel Troll")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new KraulHarpooner()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Kraul Harpooner");
    }

    @Test
    @DisplayName("A creature discarded in response can pay the upkeep exile requirement")
    void discardingInResponseFeedsUpkeep() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setHand(player1, List.of(new KraulHarpooner()));
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Charnel Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Kraul Harpooner");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Kraul Harpooner");
    }

    @Test
    @DisplayName("An opponent's graveyard cannot pay the upkeep exile requirement")
    void cannotExileFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new KraulHarpooner()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Charnel Troll");
        harness.assertInGraveyard(player2, "Kraul Harpooner");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @CardUsed({AssaultSuit.class})
    @DisplayName("Sacrifice prevention leaves Troll alive without adding a counter")
    void cannotSacrificeEquippedTrollWithoutCreatureToExile() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(troll.getId());
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Charnel Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A control change does not grant a counter when no creature was exiled")
    void noCounterWhenControlChangesAndExileIsUnpaid() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(troll);
        gd.playerBattlefields.get(player2.getId()).add(troll);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Charnel Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
