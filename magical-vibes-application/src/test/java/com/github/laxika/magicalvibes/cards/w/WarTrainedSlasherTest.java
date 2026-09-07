package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarTrainedSlasher.class})
class WarTrainedSlasherTest extends BaseCardTest {

    @Test
    void doublesPowerWhenAttackingBattle() {
        Permanent battle = addBattle(player2);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackersAt(battle);
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isEqualTo(4);
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void doesNotTriggerWhenAttackingPlayer() {
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(slasher.getPowerModifier()).isZero();
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    @Test
    void powerBoostEndsAtCleanup() {
        Permanent battle = addBattle(player2);
        Permanent slasher = addCreatureReady(player1, new WarTrainedSlasher());

        declareAttackersAt(battle);
        resolveAllTriggers();
        assertThat(slasher.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(slasher.getPowerModifier()).isZero();
        assertThat(slasher.getToughnessModifier()).isZero();
    }

    private Permanent addBattle(Player controller) {
        Card card = new Card() {};
        card.setName("Test Battle");
        card.setType(CardType.BATTLE);
        Permanent battle = harness.addToBattlefieldAndReturn(controller, card);
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
