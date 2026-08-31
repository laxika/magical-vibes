package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarabOfTheUnseen.class, Forest.class, GrizzlyBears.class, HolyStrength.class})
class ScarabOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every Aura attached to the target, including an opponent's, to its owner's hand")
    void returnsAllAttachedAuras() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        ownAura.setAttachedTo(bears.getId());

        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        opponentAura.setAttachedTo(bears.getId());

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownAura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentAura);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Sacrifices itself and schedules a draw at the next upkeep")
    void sacrificesItselfAndSchedulesDraw() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scarab);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextTurnUpkeep() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);

        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }
}
