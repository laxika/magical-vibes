package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoercivePortal.class, Forest.class, GrizzlyBears.class, Naturalize.class})
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

    @Test
    void homageMajorityDrawsExactlyOneCardOnlyForTheController() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(first, second));
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new CoercivePortal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.CoercivePortalChoice.HOMAGE);
        harness.handleListChoice(player2, ChoiceContext.CoercivePortalChoice.HOMAGE);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(portal);
    }

    @Test
    void secondPlayersPortalStartsVotingWithItsController() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(forest));
        harness.addToBattlefield(player2, new CoercivePortal());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.CoercivePortalChoice.HOMAGE);
        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.CoercivePortalChoice.CARNAGE);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Coercive Portal");
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CoercivePortal());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void triggerStillResolvesAfterPortalIsDestroyed(boolean carnage) {
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new CoercivePortal());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, portal.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Coercive Portal");
        harness.passBothPriorities();

        String vote = carnage ? ChoiceContext.CoercivePortalChoice.CARNAGE
                : ChoiceContext.CoercivePortalChoice.HOMAGE;
        harness.handleListChoice(player1, vote);
        harness.handleListChoice(player2, vote);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        if (carnage) {
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
            harness.assertInGraveyard(player2, "Grizzly Bears");
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        } else {
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        }
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.CoercivePortalChoice.OPTIONS);
        return choice;
    }
}
