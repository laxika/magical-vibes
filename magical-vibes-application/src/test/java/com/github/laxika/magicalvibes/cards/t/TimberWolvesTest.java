package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimberWolves.class, GrayOgre.class})
class TimberWolvesTest extends BaseCardTest {

    @Test
    void canBandWithOneNonBandingAttacker() {
        Permanent wolves = addCreatureReady(player1, new TimberWolves());
        Permanent nonBander = addCreatureReady(player1, new GrayOgre());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(wolves.getBandId()).isNotNull();
        assertThat(wolves.getBandId()).isEqualTo(nonBander.getBandId());
    }
}
