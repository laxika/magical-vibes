package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingGeoderm.class})
class RampagingGeodermTest extends BaseCardTest {

    @Test
    void givesTargetAttackingCreatureTemporaryBoostWhenAttackingPlayer() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();

        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(geoderm.getPowerModifier()).isEqualTo(1);
        assertThat(geoderm.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geoderm.getPowerModifier()).isZero();
        assertThat(geoderm.getToughnessModifier()).isZero();
    }

    @Test
    void putsCounterOnTargetAttackingCreatureWhenAttackingBattle() {
        Permanent battle = addBattle(player2);
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());

        declareAttackersAt(battle);
        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();

        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(geoderm.getPowerModifier()).isZero();
        assertThat(geoderm.getToughnessModifier()).isZero();
    }

    @Test
    void battleCounterRemainsAfterEndOfTurn() {
        Permanent battle = addBattle(player2);
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());

        declareAttackersAt(battle);
        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addBattle(Player controller) {
        Card card = new Card() {};
        card.setName("Test Battle");
        card.setType(CardType.BATTLE);
        Permanent battle = harness.addToBattlefieldAndReturn(controller, card);
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(player2.getId());
        return battle;
    }

    private void declareAttackersAt(Permanent battle) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
    }
}
