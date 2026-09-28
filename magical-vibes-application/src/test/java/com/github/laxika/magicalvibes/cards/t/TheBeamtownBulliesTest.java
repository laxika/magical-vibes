package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBeamtownBullies.class, GrizzlyBears.class})
class TheBeamtownBulliesTest extends BaseCardTest {

    @Test
    void putsNonlegendaryCreatureUnderActiveOpponentsControlWithHasteAndGoad() {
        Permanent bullies = addReady(new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isEqualTo(1);
        assertThat(gd.stolenCreatures).containsEntry(returned.getId(), player1.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(returned.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(bullies.isTapped()).isTrue();
    }

    @Test
    void exilesReturnedCreatureAtNextEndStep() {
        addReady(new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    void cannotTargetOpponentWhenItIsNotTheirTurn() {
        addReady(new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent whose turn it is");
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
