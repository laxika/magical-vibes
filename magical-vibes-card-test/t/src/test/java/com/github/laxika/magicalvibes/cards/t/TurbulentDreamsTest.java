package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.cards.l.Liquify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurbulentDreams.class, AngelOfRetribution.class, TaintedIsle.class, Compulsion.class, Liquify.class})
class TurbulentDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two cards and returns two target nonland permanents")
    void returnsXNonlandPermanentsForTwoDiscardedCards() {
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(firstTargetId, secondTargetId), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Turbulent Dreams");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Angel of Retribution"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Angel of Retribution"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Requires exactly X target nonland permanents")
    void requiresExactlyXTargets() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 2, List.of(targetId), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("X=0 returns no permanents and discards nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TaintedIsle()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(targetId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    void discardCostIsPaidBeforeResolutionAndNotRefundedWhenCountered() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        TurbulentDreams dreams = new TurbulentDreams();
        harness.setHand(player1, List.of(dreams, new TaintedIsle()));
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(targetId), List.of(1));

        harness.assertInGraveyard(player1, "Tainted Isle");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(gd.stack).hasSize(1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dreams.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tainted Isle");
        harness.assertOnBattlefield(player2, "Angel of Retribution");
        harness.assertNotInHand(player2, "Angel of Retribution");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(dreams.getId()));
    }

    @Test
    void canReturnOwnEnchantmentAndOpponentsCreature() {
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player1, new Compulsion()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new TaintedIsle(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(enchantmentId, creatureId), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Compulsion");
        harness.assertInHand(player2, "Angel of Retribution");
        harness.assertNotOnBattlefield(player1, "Compulsion");
        harness.assertNotOnBattlefield(player2, "Angel of Retribution");
        harness.assertNotInHand(player2, "Compulsion");
        harness.assertInGraveyard(player1, "Tainted Isle");
        harness.assertInGraveyard(player1, "Angel of Retribution");
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherTargetIsSacrificed() {
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new Compulsion()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setLibrary(player2, List.of(new TaintedIsle()));
        harness.setHand(player1, List.of(new TurbulentDreams(), new TaintedIsle(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(enchantmentId, creatureId), List.of(1, 2));
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Compulsion");
        harness.assertNotInHand(player2, "Compulsion");
        harness.assertInHand(player2, "Angel of Retribution");
        harness.assertNotOnBattlefield(player2, "Angel of Retribution");
        harness.assertInGraveyard(player1, "Turbulent Dreams");
        harness.assertInGraveyard(player1, "Tainted Isle");
        harness.assertInGraveyard(player1, "Angel of Retribution");
    }

    @Test
    void cannotDiscardFewerCardsThanX() {
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new TaintedIsle()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(
                player1, 0, 2, List.of(firstTargetId, secondTargetId), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Turbulent Dreams");
        harness.assertInHand(player1, "Tainted Isle");
    }

    @Test
    void cannotDiscardTheSpellToPayItsOwnCost() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(
                player1, 0, 1, List.of(targetId), List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Turbulent Dreams");
    }

    @Test
    void cannotTargetTheSamePermanentTwice() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new TaintedIsle(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(
                player1, 0, 2, List.of(targetId, targetId), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
