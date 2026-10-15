package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DramaticRescue;
import com.github.laxika.magicalvibes.cards.m.MizziumMortars;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivmagusElemental.class, Shock.class, MizziumMortars.class, NivixGuildmage.class, DramaticRescue.class})
class NivmagusElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a controlled instant from the stack and puts two counters on Nivmagus Elemental")
    void exilesControlledInstantAndAddsCounters() {
        Permanent nivmagus = addCreatureReady(player1, new NivmagusElemental());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileInstantOrSorcerySpellCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).containsExactly(shock.getId());

        harness.passBothPriorities();

        assertThat(nivmagus.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot exile an instant controlled by another player")
    void cannotExileOpponentSpell() {
        harness.addToBattlefield(player1, new NivmagusElemental());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(shock);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Nivmagus can exile a sorcery and gains counters only on resolution")
    void exilesSorceryWhileTappedAndSummoningSick() {
        Permanent nivmagus = harness.addToBattlefieldAndReturn(player1, new NivmagusElemental());
        nivmagus.setSummoningSick(true);
        nivmagus.tap();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NivmagusElemental());
        MizziumMortars mortars = new MizziumMortars();
        harness.setHand(player1, List.of(mortars));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, opponentCreature.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(mortars.getId()));

        assertThat(nivmagus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).containsExactly(mortars.getId());
        harness.passBothPriorities();

        assertThat(nivmagus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(nivmagus.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards in hand and graveyard cannot pay the spell exile cost")
    void cannotExileCardsOutsideTheStack() {
        harness.addToBattlefield(player1, new NivmagusElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.setGraveyard(player1, List.of(new MizziumMortars()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell cannot pay the spell exile cost")
    void cannotExileCreatureSpell() {
        harness.addToBattlefield(player1, new NivmagusElemental());
        NivmagusElemental creatureSpell = new NivmagusElemental();
        harness.setHand(player1, List.of(creatureSpell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(creatureSpell);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A controlled spell copy can be exiled without exiling the original card")
    void exilesSpellCopyButLeavesOriginalOnStack() {
        Permanent nivmagus = harness.addToBattlefieldAndReturn(player1, new NivmagusElemental());
        harness.addToBattlefield(player1, new NivixGuildmage());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NivmagusElemental());
        MizziumMortars mortars = new MizziumMortars();
        harness.setHand(player1, List.of(mortars));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, opponentCreature.getId());
        harness.activateAbility(player1, 1, 1, null, mortars.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(copy.getCard().getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(mortars);
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();
        assertThat(nivmagus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mortars);
    }

    @Test
    @DisplayName("Removing Nivmagus in response does not refund the exiled spell")
    void sourceLeavingBattlefieldDoesNotRefundCost() {
        Permanent nivmagus = harness.addToBattlefieldAndReturn(player1, new NivmagusElemental());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new DramaticRescue()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.castInstant(player1, 0, nivmagus.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Nivmagus Elemental");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).containsExactly(shock.getId());
        assertThat(nivmagus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 20);
    }
}
