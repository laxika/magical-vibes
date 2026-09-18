package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelistsHeritage.class, GrizzlyBears.class})
class DuelistsHeritageTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever one or more creatures attack, an attacking creature may gain double strike")
    void grantsDoubleStrikeToTargetAttacker() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Declining the optional keyword grant does nothing")
    void mayDeclineGrant() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The keyword grant wears off at end of turn")
    void grantWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DuelistsHeritage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
