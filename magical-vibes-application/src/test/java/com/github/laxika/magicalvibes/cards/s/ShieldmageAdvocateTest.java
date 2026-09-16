package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldmageAdvocate.class, BorderPatrol.class})
class ShieldmageAdvocateTest extends BaseCardTest {

    @Test
    void returnsOpponentsGraveyardCardAndPreventsAllDamageToPlayer() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BorderPatrol();
        Permanent source = addReadyCreatureWithStats(player2, 5, 5);
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setLife(player1, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(returnedCard.getId(), player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(returnedCard.getId()));

        source.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventsAllDamageToTargetCreature() {
        Permanent target = addReadyCreatureWithStats(player1, 2, 2);
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BorderPatrol();
        Permanent source = addReadyCreatureWithStats(player2, 3, 3);
        harness.setGraveyard(player2, List.of(returnedCard));

        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of(returnedCard.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void preventsDamageFromChosenSourceOnly() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Permanent chosenSource = addReadyCreatureWithStats(player1, 3, 3);
        Permanent otherSource = addReadyCreatureWithStats(player1, 2, 2);
        Card returnedCard = new BorderPatrol();
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setLife(player2, 20);

        harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(returnedCard.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCreatureWithStats(Player player, int power, int toughness) {
        BorderPatrol card = new BorderPatrol();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
