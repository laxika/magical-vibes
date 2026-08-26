package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldmageAdvocate.class, GrizzlyBears.class})
class ShieldmageAdvocateTest extends BaseCardTest {

    @Test
    void returnsOpponentsGraveyardCardAndPreventsAllDamageToPlayer() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new GrizzlyBears();
        Permanent source = addReadyCreatureWithStats(player2, 5, 5);
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setLife(player1, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(returnedCard.getId(), player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(returnedCard.getId()));

        harness.forceActivePlayer(player2);
        source.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventsAllDamageToTargetCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new GrizzlyBears();
        Permanent source = addReadyCreatureWithStats(player2, 3, 3);
        harness.setGraveyard(player2, List.of(returnedCard));

        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of(returnedCard.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceActivePlayer(player2);
        source.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    private Permanent addReadyCreatureWithStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
