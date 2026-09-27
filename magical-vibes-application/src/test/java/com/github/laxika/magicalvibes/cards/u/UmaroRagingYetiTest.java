package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaroRagingYeti.class, GrizzlyBears.class})
class UmaroRagingYetiTest extends BaseCardTest {

    @Test
    void randomlyAppliesEachMode() {
        Player controller = player1;
        Player opponent = player2;
        Permanent umaro = harness.addToBattlefieldAndReturn(controller, new UmaroRagingYeti());
        Permanent bear = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        harness.setHand(controller, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(controller, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Set<String> modes = new HashSet<>();
        for (int i = 0; i < 90 && modes.size() < 3; i++) {
            int bearPower = bear.getEffectivePower();
            int graveyardSize = gd.playerGraveyards.get(controller.getId()).size();
            int opponentLife = gd.playerLifeTotals.get(opponent.getId());

            harness.forceActivePlayer(controller);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
                harness.handlePermanentChosen(controller, opponent.getId());
            }
            harness.resolveAllTriggers();

            if (bear.getEffectivePower() > bearPower) {
                modes.add("pump");
            }
            if (gd.playerGraveyards.get(controller.getId()).size() > graveyardSize) {
                modes.add("discard-and-draw");
            }
            if (gd.playerLifeTotals.get(opponent.getId()) < opponentLife) {
                modes.add("damage");
            }
        }

        assertThat(modes).containsExactlyInAnyOrder("pump", "discard-and-draw", "damage");
        assertThat(umaro.getEffectivePower()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
    }
}
