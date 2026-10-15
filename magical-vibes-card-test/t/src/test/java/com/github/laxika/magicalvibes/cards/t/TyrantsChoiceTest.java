package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrantsChoice.class, GrizzlyBears.class})
class TyrantsChoiceTest extends BaseCardTest {

    @Test
    void tortureWinsOnTieAndEachOpponentLosesFourLife() {
        cast();

        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.TORTURE);
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.DEATH);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void deathMajorityMakesEachOpponentSacrificeAChosenCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast();

        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.DEATH);
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.DEATH);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void unanimousTortureLeavesCreaturesAndControllerUnaffected() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.TORTURE);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.TORTURE);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void reverseTieAlsoCausesLifeLoss() {
        cast();
        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.DEATH);
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.TORTURE);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void deathWithoutOpposingCreaturesLeavesControllerUnaffected() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast();
        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.DEATH);
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.DEATH);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(own);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Tyrant's Choice");
    }

    private void cast() {
        harness.castFromHand(player1, new TyrantsChoice(), "{1}{B}");
        harness.passBothPriorities();
    }
}
