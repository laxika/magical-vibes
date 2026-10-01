package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hobble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaimaTheFracturedCalm.class, GrizzlyBears.class, Hobble.class})
class KaimaTheFracturedCalmTest extends BaseCardTest {

    @Test
    void goadsOpponentCreaturesEnchantedByYourAurasAndAddsCountersForEach() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent enchantedOpponent = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, enchantedOpponent);
        Permanent opponentWithOpponentsAura = addCreatureReady(player2, new GrizzlyBears());
        addAura(player2, opponentWithOpponentsAura);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, ownCreature);

        resolveControllerEndStep();

        assertThat(als.getMustAttackRequirementCount(gd, enchantedOpponent)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, opponentWithOpponentsAura)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, ownCreature)).isZero();
        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void goadExpiresAtKaimaControllersNextTurn() {
        addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent enchantedOpponent = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, enchantedOpponent);

        resolveControllerEndStep();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(als.getMustAttackRequirementCount(gd, enchantedOpponent)).isZero();
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Hobble());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
