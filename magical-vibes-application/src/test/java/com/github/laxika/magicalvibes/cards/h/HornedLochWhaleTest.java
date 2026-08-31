package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LagoonBreach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornedLochWhale.class, LagoonBreach.class, GrizzlyBears.class, Island.class})
class HornedLochWhaleTest extends BaseCardTest {

    @Test
    void adventurePutsAnOpponentsAttackingCreatureOnTheBottomOfItsOwnersLibrary() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        HornedLochWhale card = new HornedLochWhale();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard, attacker.getCard());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetAnAttackingCreatureYouControl() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        harness.setHand(player1, List.of(new HornedLochWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whaleEntersUntappedDuringItsControllersTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castWhale();

        Permanent whale = findPermanent(player1, "Horned Loch-Whale");
        assertThat(whale.isTapped()).isFalse();
    }

    @Test
    void whaleEntersTappedDuringAnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.passPriority(gd, player2);
        castWhale();

        Permanent whale = findPermanent(player1, "Horned Loch-Whale");
        assertThat(whale.isTapped()).isTrue();
    }

    private void castWhale() {
        harness.setHand(player1, List.of(new HornedLochWhale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
