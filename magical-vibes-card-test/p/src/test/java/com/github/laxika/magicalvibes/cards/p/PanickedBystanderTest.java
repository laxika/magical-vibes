package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CacklingCulprit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PanickedBystander.class, CacklingCulprit.class, GrizzlyBears.class, Terminate.class})
class PanickedBystanderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control dies")
    void gainsLifeWhenAllyDies() {
        harness.addToBattlefield(player1, new PanickedBystander());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroyCreature(player1, bear);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gains 1 life when it dies")
    void gainsLifeWhenItDies() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        harness.setLife(player1, 20);

        destroyCreature(player1, bystander);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Transforms at its controller's end step after 3 life is gained")
    void transformsAfterGainingThreeLife() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bystander.isTransformed()).isTrue();
        assertThat(bystander.getCard()).isInstanceOf(CacklingCulprit.class);
    }

    @Test
    @DisplayName("Does not transform when its controller gained less than 3 life")
    void doesNotTransformBelowLifeThreshold() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bystander.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Back face gains deathtouch until end of turn")
    void backFaceGainsDeathtouch() {
        Permanent culprit = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        transformToBack(culprit);
        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, culprit), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, culprit, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void seesItsOwnAndAllyDeathWhenTheyDieSimultaneously() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        bystander.setMarkedDamage(2);
        ally.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void transformsAfterThreeActualLifeGainsEvenWithLowerLifeTotal() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        harness.setLife(player1, 20);
        for (int i = 0; i < 3; i++) {
            Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            destroyCreature(player1, ally);
            harness.passBothPriorities();
        }
        harness.assertLife(player1, 23);
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bystander.isTransformed()).isTrue();
        harness.assertLife(player1, 10);
    }

    @Test
    void backFaceGainsLifeWhenAllyDies() {
        Permanent culprit = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        transformToBack(culprit);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroyCreature(player1, ally);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void backFaceGainsLifeWhenItDies() {
        Permanent culprit = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        transformToBack(culprit);
        harness.setLife(player1, 20);

        destroyCreature(player1, culprit);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void neitherFaceGainsLifeWhenOpponentCreatureDies() {
        Permanent culprit = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        transformToBack(culprit);
        harness.addToBattlefield(player1, new PanickedBystander());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroyCreature(player1, opponentCreature);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTransformAtOpponentsEndStep() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(bystander.isTransformed()).isFalse();
    }

    @Test
    void gainingThirdLifeDuringEndStepDoesNotCreateTransformTrigger() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ally.getId());
        harness.passBothPriorities();

        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(3);
        assertThat(bystander.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void backFaceDeathtouchExpiresAtCleanup() {
        Permanent culprit = harness.addToBattlefieldAndReturn(player1, new PanickedBystander());
        transformToBack(culprit);
        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, culprit), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, culprit, Keyword.DEATHTOUCH)).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, culprit, Keyword.DEATHTOUCH)).isFalse();
        assertThat(culprit.isTransformed()).isTrue();
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Terminate()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void transformToBack(Permanent permanent) {
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(permanent.isTransformed()).isTrue();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
