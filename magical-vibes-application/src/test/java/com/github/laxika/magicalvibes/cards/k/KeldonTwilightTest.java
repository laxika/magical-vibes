package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonTwilight.class, AncientSpider.class})
class KeldonTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses a creature controlled since the turn began to sacrifice")
    void activePlayerChoosesEligibleCreatureToSacrifice() {
        harness.addToBattlefield(player1, new KeldonTwilight());
        Permanent kept = addCreatureReady(player2, new AncientSpider());
        Permanent sacrificed = addCreatureReady(player2, new AncientSpider());
        Permanent newlyControlled = harness.addToBattlefieldAndReturn(player2, new AncientSpider());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(kept, newlyControlled)
                .doesNotContain(sacrificed);
    }

    @Test
    @DisplayName("Does not trigger if any creature attacked this turn")
    void doesNotTriggerAfterAnyCreatureAttacked() {
        harness.addToBattlefield(player1, new KeldonTwilight());
        Permanent attacker = addCreatureReady(player2, new AncientSpider());

        declareAttackers(player2, List.of(0));
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Does not sacrifice a creature that came under the player's control this turn")
    void excludesCreaturesGainedThisTurn() {
        harness.addToBattlefield(player1, new KeldonTwilight());
        Permanent newlyControlled = harness.addToBattlefieldAndReturn(player1, new AncientSpider());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(newlyControlled);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
