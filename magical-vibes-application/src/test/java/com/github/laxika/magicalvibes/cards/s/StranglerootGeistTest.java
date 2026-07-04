package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StranglerootGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackWithSummoningSicknessDueToHaste() {
        harness.setHand(player1, List.of(new StranglerootGeist()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        GameService gs = harness.getGameService();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        Permanent geist = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(geist.getCard().getName()).isEqualTo("Strangleroot Geist");
        assertThat(geist.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Undying returns Strangleroot Geist with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, geist.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedGeist = findPermanent(player1, "Strangleroot Geist");
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Strangleroot Geist"));
    }

    @Test
    @DisplayName("Undying does not return Strangleroot Geist when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        geist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, geist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Strangleroot Geist"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Strangleroot Geist"));
        assertThat(gd.stack).isEmpty();
    }
}
