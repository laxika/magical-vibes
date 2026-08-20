package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BroodingSaurianTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, each player regains nontoken permanents they own")
    void returnsOwnedNontokenPermanentsAtEachEndStep() {
        harness.addToBattlefield(player1, new BroodingSaurian());
        Permanent player2Permanent = addStolenPermanent(player1, player2);
        Permanent player1Permanent = addStolenPermanent(player2, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Permanent);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player2Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player1Permanent);
    }

    @Test
    @DisplayName("Does not return token permanents")
    void doesNotReturnTokens() {
        harness.addToBattlefield(player1, new BroodingSaurian());
        Card tokenCard = new Card();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        gd.stolenCreatures.put(token.getId(), player2.getId());
        assertThat(token.getCard().isToken()).isTrue();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), token,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    private Permanent addStolenPermanent(Player controller, Player owner) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new Forest());
        gd.stolenCreatures.put(permanent.getId(), owner.getId());
        return permanent;
    }
}
