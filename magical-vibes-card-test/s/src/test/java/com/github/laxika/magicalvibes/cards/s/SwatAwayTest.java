package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwatAway.class, GrizzlyBears.class, Island.class, Shock.class})
class SwatAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {U}{U} while a creature is attacking you")
    void reducedCostWhileCreatureAttacksYou() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Costs {2}{U}{U} when no creature is attacking you")
    void fullCostWhenNotAttacked() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The target creature's owner can put it on the bottom of their library")
    void targetCreatureOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        castSwatAway(target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard, target.getCard());
        harness.assertInGraveyard(player1, "Swat Away");
    }

    @Test
    @DisplayName("The target spell's owner can leave it on top of their library")
    void targetSpellOwnerChoosesTop() {
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = shock.getId();
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, shockId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(shock, topCard);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Swat Away");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking creature does not reduce the cost when it attacks the opponent")
    void fullCostWhenAttackingOpponent() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple attackers reduce only the generic cost once")
    void multipleAttackersDoNotReduceColoredCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setAttacking(true);
        first.setAttackTarget(player1.getId());
        second.setAttacking(true);
        second.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, first.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A stolen creature's owner chooses top and receives it in their library")
    void creatureOwnerChoosesTopInsteadOfController() {
        GrizzlyBears creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());

        castSwatAway(target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player1.getId());
        assertThatThrownBy(() -> harness.handleListChoice(player2, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swat Away");
    }

    @Test
    @DisplayName("Can put your own spell on the bottom without resolving it")
    void ownSpellCanGoToBottom() {
        Shock shock = new Shock();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(shock, new SwatAway()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, shock);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, lifeBefore);
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Swat Away");
    }

    @Test
    @DisplayName("Can put a creature spell on the bottom without it entering the battlefield")
    void creatureSpellCanGoToBottom() {
        GrizzlyBears creature = new GrizzlyBears();
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player2, List.of(creature));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.forceActivePlayer(player1);

        castSwatAway(creature.getId());
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, creature);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swat Away");
    }

    @Test
    @DisplayName("Does not move a creature that dies before resolution")
    void targetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swat Away");
    }

    private void castSwatAway(UUID targetId) {
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

}
