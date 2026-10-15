package com.github.laxika.magicalvibes.cards.b;

import java.util.List;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HuntedTroll;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodingSaurian.class, Forest.class, HuntedTroll.class})
class BroodingSaurianTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, each player regains nontoken permanents they own")
    void returnsOwnedNontokenPermanentsAtEachEndStep() {
        harness.addToBattlefield(player1, new BroodingSaurian());
        Permanent player2Permanent = addStolenPermanent(player1, player2);
        Permanent player1Permanent = addStolenPermanent(player2, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Permanent);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player2Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player1Permanent);
    }

    @Test
    @DisplayName("Does not return token permanents")
    void doesNotReturnTokens() {
        harness.addToBattlefield(player1, new BroodingSaurian());
        harness.enterBattlefieldAndReturn(player1, new HuntedTroll());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        Permanent token = findPermanents(player2, "Faerie").getFirst();
        gd.stolenCreatures.put(token.getId(), player2.getId());
        assertThat(token.getCard().isToken()).isTrue();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), token,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("A stolen Saurian returns itself at its controller's end step and overrides earlier control effects")
    void stolenSaurianReturnsItselfPermanently() {
        BroodingSaurian card = new BroodingSaurian();
        card.setOwnerId(player1.getId());
        Permanent saurian = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), saurian,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saurian);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saurian);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(saurian);

        harness.passUntilWithNoAttackers(null, TurnStep.CLEANUP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saurian);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(saurian);
    }

    @Test
    @DisplayName("The trigger survives source removal and uses ownership at resolution")
    void resolvesWithoutSourceAndReturnsPermanentStolenAfterTriggering() {
        Permanent saurian = harness.addToBattlefieldAndReturn(player1, new BroodingSaurian());
        Forest landCard = new Forest();
        landCard.setOwnerId(player1.getId());
        Permanent stolenLand = harness.addToBattlefieldAndReturn(player1, landCard);
        stolenLand.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saurian));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), stolenLand,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(stolenLand);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolenLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(stolenLand);
        assertThat(stolenLand.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Brooding Saurian");
    }

    private Permanent addStolenPermanent(Player controller, Player owner) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new Forest());
        gd.stolenCreatures.put(permanent.getId(), owner.getId());
        return permanent;
    }
}
