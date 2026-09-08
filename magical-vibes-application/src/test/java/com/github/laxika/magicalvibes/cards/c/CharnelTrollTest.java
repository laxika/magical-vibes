package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharnelTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card from the graveyard puts a +1/+1 counter on Charnel Troll")
    void exilingCreatureFromGraveyardAddsCounter() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new Mountain(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The upkeep trigger chooses among all creature cards in the graveyard")
    void choosesCreatureCardFromGraveyard() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Without a creature card in the graveyard the upkeep trigger sacrifices Charnel Troll")
    void sacrificesWithoutCreatureCard() {
        harness.addToBattlefield(player1, new CharnelTroll());
        harness.setGraveyard(player1, List.of(new Mountain()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Charnel Troll");
        harness.assertInGraveyard(player1, "Charnel Troll");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Paying the activated ability cost with a creature card puts a +1/+1 counter on Charnel Troll")
    void activatedAbilityDiscardsCreatureAndAddsCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CharnelTroll());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The activated ability cannot discard a noncreature card")
    void activatedAbilityRequiresCreatureCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new CharnelTroll());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
