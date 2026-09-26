package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaryZoneGuard.class, Forest.class, GrizzlyBears.class})
class WaryZoneGuardTest extends BaseCardTest {

    @Test
    void entersTappedAndSurvivalReturnsLandAndPerpetuallyBoostsGuard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear, forest));

        assertThat(guard.isTapped()).isTrue();

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(forest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId())
                        && !permanent.isTapped());
    }

    @Test
    void survivalCanBeDeclinedWhenNoLandIsInGraveyard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
    }

    @Test
    void untappedGuardDoesNotTriggerSurvival() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        guard.untap();
        harness.setGraveyard(player1, List.of(new Forest()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    @Test
    void untappingBeforeResolutionPreventsReturnAndGrowth() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        guard.untap();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
