package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfOpulence.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfOpulenceTest extends BaseCardTest {

    @Test
    @DisplayName("The Curse's controller and the attacking player each get a Gold")
    void createsGoldForCurseControllerAndAttackingPlayer() {
        placeCurseOnPlayer2();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(goldTokens(player1)).hasSize(2);
        assertThat(goldTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Attacking the enchanted player's planeswalker does not trigger")
    void attackingEnchantedPlayersPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer2();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1), Map.of(1, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(goldTokens(player1)).isEmpty();
        assertThat(goldTokens(player2)).isEmpty();
    }

    private void placeCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfOpulence());
        curse.setAttachedTo(player2.getId());
    }

    private List<Permanent> goldTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Gold")
                        && permanent.getCard().getSubtypes().isEmpty())
                .toList();
    }
}
