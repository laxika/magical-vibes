package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XantidSwarm.class, Shock.class})
class XantidSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger prevents the defending player from casting spells this turn")
    void attackTriggerSilencesDefendingPlayer() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new XantidSwarm());
        swarm.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack trigger does not prevent its controller from casting spells")
    void attackTriggerDoesNotSilenceController() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new XantidSwarm());
        swarm.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }
}
