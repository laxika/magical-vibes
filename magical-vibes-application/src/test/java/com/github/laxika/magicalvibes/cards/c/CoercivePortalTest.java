package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoercivePortal.class, Forest.class, GrizzlyBears.class})
class CoercivePortalTest extends BaseCardTest {

    @Test
    void tiedVoteDrawsACardAndKeepsThePortal() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new CoercivePortal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CoercivePortalChoice.CARNAGE);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CoercivePortalChoice.HOMAGE);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(portal);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    void carnageMajoritySacrificesPortalAndDestroysNonlands() {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new CoercivePortal());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.CoercivePortalChoice.CARNAGE);
        harness.handleListChoice(player2, ChoiceContext.CoercivePortalChoice.CARNAGE);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(portal, player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Land);
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.CoercivePortalChoice.OPTIONS);
        return choice;
    }
}
