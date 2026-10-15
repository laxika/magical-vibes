package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soulherder.class, GrizzlyBears.class, Forest.class})
class SoulherderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when any creature is exiled from the battlefield")
    void growsWhenOpponentCreatureIsExiled() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToExile(bears);

        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("May flicker another creature you control at your end step")
    void mayFlickerAnotherCreatureAtEndStep() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bearsId);
        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Cannot target Soulherder itself or an opponent's creature")
    void targetMustBeAnotherCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Soulherder")))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Declining the flicker leaves the creature and counters unchanged")
    void decliningFlickerDoesNothing() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(bears.getId());
        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A stolen creature returns immediately under its owner's control")
    void stolenCreatureReturnsToOwner() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
        harness.passBothPriorities();
        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Exiling a noncreature permanent does not cause growth")
    void exilingLandDoesNotCauseGrowth() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        removeToExile(forest);

        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The flicker ability does not trigger during the opponent's end step")
    void noFlickerAtOpponentEndStep() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(bears.getId());
        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The end step proceeds when there is no other creature to target")
    void noEligibleCreatureAtEndStep() {
        harness.addToBattlefield(player1, new Soulherder());

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flickering removes counters and tapped state from the returned creature")
    void flickerReturnsFreshPermanent() {
        harness.addToBattlefield(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.tap();

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
