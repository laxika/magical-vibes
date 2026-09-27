package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IAmDuskmourn.class, DarkRitual.class})
class IAmDuskmournTest extends BaseCardTest {

    @Test
    void mayCastSpellFromHandForFreeThenAbandonsScheme() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of(new DarkRitual()));

        resolveControllerEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scheme.getCard());
    }

    @Test
    void decliningFreeCastKeepsSchemeAndCardInHand() {
        Permanent scheme = addScheme();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));

        resolveControllerEndStep();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ritual);
    }

    private Permanent addScheme() {
        return harness.addToBattlefieldAndReturn(player1, new IAmDuskmourn());
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
