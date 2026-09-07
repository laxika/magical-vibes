package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormfangTurtle.class, Island.class})
class WormfangTurtleTest extends BaseCardTest {

    @Test
    void exilesOneLandYouControlAndReturnsItWhenTurtleLeaves() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new WormfangTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, secondLand.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(secondLand.getCard().getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(firstLand.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondLand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentLand);

        Permanent turtle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WormfangTurtle)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turtle));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondLand.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(secondLand.getCard().getId()));
    }

    @Test
    void doesNothingWhenYouControlNoLand() {
        harness.setHand(player1, List.of(new WormfangTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
    }
}
