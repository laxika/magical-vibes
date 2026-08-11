package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        setDeck(player2, List.of(topCard, nextCard));

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
        setDeck(player2, List.of(topCard));
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

    private void castSwatAway(UUID targetId) {
        harness.setHand(player1, List.of(new SwatAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
