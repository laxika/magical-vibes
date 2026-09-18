package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenArgosy.class, GrizzlyBears.class})
class GoldenArgosyTest extends BaseCardTest {

    @Test
    void attacksExileCreaturesThatCrewedItAndReturnThemTappedAtNextEndStep() {
        Permanent argosy = harness.addToBattlefieldAndReturn(player1, new GoldenArgosy());
        argosy.setSummoningSick(false);
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.PermanentChoice crewChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (crewChoice != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crewer);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(argosy, bystander);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .hasSize(2)
                .anyMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(argosy, bystander);
    }
}
