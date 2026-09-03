package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterceptorMechan.class, AetherSpellbomb.class, GrizzlyBears.class, HolyDay.class, Forest.class})
class InterceptorMechanTest extends BaseCardTest {

    @Test
    void etbReturnsArtifactOrCreatureCardToHand() {
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spellbomb, bears));

        castMechan();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(spellbomb.getId(), bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spellbomb.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aether Spellbomb");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void etbDoesNotTargetNonArtifactNonCreatureCard() {
        HolyDay nonPermanent = new HolyDay();
        harness.setGraveyard(player1, List.of(nonPermanent));

        castMechan();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    void putsCounterAtEndStepAfterNonlandPermanentLeaves() {
        Permanent mechan = addReadyMechan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterAfterOnlyLandLeaves() {
        Permanent mechan = addReadyMechan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castMechan() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new InterceptorMechan()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyMechan() {
        Permanent mechan = harness.addToBattlefieldAndReturn(player1, new InterceptorMechan());
        mechan.setSummoningSick(false);
        return mechan;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
