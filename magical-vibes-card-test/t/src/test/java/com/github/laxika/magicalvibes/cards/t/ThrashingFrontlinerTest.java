package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.InvasionOfErgamon;
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

@CardUsed({ThrashingFrontliner.class, InvasionOfErgamon.class})
class ThrashingFrontlinerTest extends BaseCardTest {

    @Test
    void getsPlusOnePlusOneWhenAttackingBattle() {
        Permanent battle = addBattle(player2);
        Permanent frontliner = addCreatureReady(player1, new ThrashingFrontliner());

        declareAttackersAt(battle);
        resolveAllTriggers();

        assertThat(frontliner.getPowerModifier()).isEqualTo(1);
        assertThat(frontliner.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenAttackingPlayer() {
        Permanent frontliner = addCreatureReady(player1, new ThrashingFrontliner());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(frontliner.getPowerModifier()).isZero();
        assertThat(frontliner.getToughnessModifier()).isZero();
    }

    @Test
    void battleBoostEndsAtCleanup() {
        Permanent battle = addBattle(player2);
        Permanent frontliner = addCreatureReady(player1, new ThrashingFrontliner());

        declareAttackersAt(battle);
        resolveAllTriggers();
        assertThat(frontliner.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(frontliner.getPowerModifier()).isZero();
        assertThat(frontliner.getToughnessModifier()).isZero();
    }

    @Test
    void boostResolvesEvenIfAttackedBattleLeavesBattlefield() {
        Permanent frontliner = addCreatureReady(player1, new ThrashingFrontliner());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfErgamon());
        battle.setProtectorPlayerId(player2.getId());

        declareAttackersAt(battle);
        assertThat(gd.stack).hasSize(1);
        assertThat(frontliner.getPowerModifier()).isZero();
        assertThat(frontliner.getToughnessModifier()).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(battle);
        gd.playerGraveyards.get(player1.getId()).add(battle.getCard());
        resolveAllTriggers();

        assertThat(frontliner.getPowerModifier()).isEqualTo(1);
        assertThat(frontliner.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void getsBoostWhenAttackingOwnBattleProtectedByOpponent() {
        Permanent frontliner = addCreatureReady(player1, new ThrashingFrontliner());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfErgamon());
        battle.setProtectorPlayerId(player2.getId());

        declareAttackersAt(battle);
        resolveAllTriggers();

        assertThat(frontliner.getPowerModifier()).isEqualTo(1);
        assertThat(frontliner.getToughnessModifier()).isEqualTo(1);
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
