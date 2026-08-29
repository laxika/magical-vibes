package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduSiegebreaker.class, GrizzlyBears.class})
class MarduSiegebreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling up to one other creature you control")
    void entersAndExilesAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mardu Siegebreaker");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        assertThat(gd.getCardsExiledByPermanent(source.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not exile the target if it leaves before the enters ability resolves")
    void doesNotExileWhenSourceLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creates a tapped attacking copy for each opponent and sacrifices it at the next end step")
    void createsAttackingCopyForEachOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        source.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Grizzly Bears");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.get(0);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private void castMarduSiegebreaker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MarduSiegebreaker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
