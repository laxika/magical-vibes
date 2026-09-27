package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoneshakerShaman.class, BorosSwiftblade.class, Forest.class, Mountain.class, Plains.class})
class StoneshakerShamanTest extends BaseCardTest {

    @Test
    @DisplayName("The active end-step player chooses an untapped land to sacrifice")
    void activePlayerChoosesUntappedLandToSacrifice() {
        harness.addToBattlefield(player1, new StoneshakerShaman());
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.tapPermanent(player2, 0);

        resolveEndStep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(kept.getId(), sacrificed.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(tapped, kept)
                .doesNotContain(sacrificed);
    }

    @Test
    @DisplayName("Does not sacrifice a tapped land when no untapped land is available")
    void doesNotSacrificeTappedLandWhenNoUntappedLandExists() {
        harness.addToBattlefield(player1, new StoneshakerShaman());
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.tapPermanent(player2, 0);

        resolveEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tapped);
    }

    @Test
    @DisplayName("The controller also sacrifices an untapped land during their own end step")
    void controllerSacrificesUntappedLandDuringOwnEndStep() {
        harness.addToBattlefield(player1, new StoneshakerShaman());
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Plains());

        resolveEndStep(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(kept.getId(), sacrificed.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificed.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(kept)
                .doesNotContain(sacrificed);
    }

    @Test
    @DisplayName("A nonland permanent is not eligible for the sacrifice")
    void nonlandPermanentIsNotEligible() {
        harness.addToBattlefield(player1, new StoneshakerShaman());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosSwiftblade());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(creature)
                .doesNotContain(land);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
    }
}
