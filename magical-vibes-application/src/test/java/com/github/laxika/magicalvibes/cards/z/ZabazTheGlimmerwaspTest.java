package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.s.ServantOfTheScale;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZabazTheGlimmerwasp.class, ArcboundWorker.class, CopperMyr.class, ServantOfTheScale.class, Shock.class})
class ZabazTheGlimmerwaspTest extends BaseCardTest {

    @Test
    void modularDeathTriggerGetsAnAdditionalCounter() {
        Permanent replacementSource = addReadyZabaz();
        Permanent dyingModularCreature = harness.addToBattlefieldAndReturn(player1, new ArcboundWorker());
        dyingModularCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent recipient = addCreatureReady(player1, new CopperMyr());

        destroyWithShock(dyingModularCreature);
        chooseDeathTriggerTarget(recipient, true);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void replacementDoesNotAffectNonModularCounterTriggers() {
        addReadyZabaz();
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTheScale());
        servant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent recipient = addCreatureReady(player1, new CopperMyr());

        destroyWithShock(servant);
        chooseDeathTriggerTarget(recipient, false);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersWithOnlyItsModularCounter() {
        harness.setHand(player1, List.of(new ZabazTheGlimmerwasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zabaz = findPermanent(player1, "Zabaz, the Glimmerwasp");

        assertThat(zabaz.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void redAbilityDestroysAnArtifactYouControl() {
        Permanent zabaz = addReadyZabaz();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Copper Myr");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zabaz);
    }

    @Test
    void redAbilityCannotTargetAnOpponentsArtifact() {
        addReadyZabaz();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whiteAbilityGrantsFlyingUntilEndOfTurn() {
        Permanent zabaz = addReadyZabaz();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, zabaz, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, zabaz, Keyword.FLYING)).isFalse();
    }

    private void destroyWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyZabaz() {
        Permanent zabaz = harness.addToBattlefieldAndReturn(player1, new ZabazTheGlimmerwasp());
        zabaz.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return zabaz;
    }

    private void chooseDeathTriggerTarget(Permanent target, boolean mayAbility) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        if (mayAbility) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
