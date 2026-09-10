package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggConscripts.class, TrainedArmodon.class, DarkRitual.class})
class MoggConscriptsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when no creature spell was cast this turn")
    void cannotAttackWithoutCreatureSpell() {
        addCreatureReady(player1, new MoggConscripts());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack after casting a creature spell this turn")
    void canAttackAfterCastingCreatureSpell() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoggConscripts());
        harness.castFromHand(player1, new TrainedArmodon(), "{1}{G}{G}");

        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Casting Mogg Conscripts itself counts as casting a creature spell")
    void canAttackAfterCastingItself() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new MoggConscripts(), "{R}");
        harness.passBothPriorities();

        Permanent conscripts = findPermanents(player1, "Mogg Conscripts").getFirst();
        conscripts.setSummoningSick(false);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("A creature spell cast by an opponent does not lift the attack restriction")
    void opponentsCreatureSpellDoesNotLiftRestriction() {
        addCreatureReady(player1, new MoggConscripts());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TrainedArmodon(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not lift the attack restriction")
    void noncreatureSpellDoesNotLiftRestriction() {
        addCreatureReady(player1, new MoggConscripts());
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
