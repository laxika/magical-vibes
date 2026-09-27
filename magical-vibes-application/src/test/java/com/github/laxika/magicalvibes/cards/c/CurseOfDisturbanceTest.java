package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfDisturbance.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfDisturbanceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie for the curse controller and the attacking player")
    void createsZombieForBothPlayersWhenEnchantedPlayerIsAttacked() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(findTokens(player1)).hasSize(1);
        assertThat(findTokens(player2)).hasSize(1);
        assertThat(findTokens(player1).getFirst().getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(findTokens(player1).getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(findTokens(player1).getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(findTokens(player1).getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(findTokens(player1).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when a planeswalker is attacked")
    void doesNotTriggerForPlaneswalkerAttack() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), java.util.Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(findTokens(player1)).isEmpty();
        assertThat(findTokens(player2)).isEmpty();
    }

    private void placeCurseOnPlayer1() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfDisturbance());
        curse.setAttachedTo(player1.getId());
    }

    private List<Permanent> findTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
