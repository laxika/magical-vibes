package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZurgoBellstriker.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class ZurgoBellstrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Zurgo can block a creature with power 1")
    void canBlockPowerOne() {
        Permanent zurgo = addCreatureReady(player2, new ZurgoBellstriker());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        wizard.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(zurgo),
                gd.playerBattlefields.get(player1.getId()).indexOf(wizard))));

        assertThat(zurgo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Zurgo can't block a creature with power 2 or greater")
    void cannotBlockPowerTwoOrGreater() {
        Permanent zurgo = addCreatureReady(player2, new ZurgoBellstriker());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(zurgo),
                gd.playerBattlefields.get(player1.getId()).indexOf(bears)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A normal cast does not return Zurgo to its owner's hand at end step")
    void normalCastDoesNotReturnAtEndStep() {
        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zurgo = findPermanent(player1, "Zurgo Bellstriker");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zurgo Bellstriker")).isSameAs(zurgo);
    }

    @Test
    @DisplayName("Dash grants haste and returns Zurgo to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zurgo = findPermanent(player1, "Zurgo Bellstriker");
        assertThat(zurgo.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(zurgo.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Zurgo Bellstriker");
        harness.assertNotOnBattlefield(player1, "Zurgo Bellstriker");
    }
}
