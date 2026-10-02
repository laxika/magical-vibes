package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LandrovalHorizonWitness.class, AirElemental.class, GrizzlyBears.class})
class LandrovalHorizonWitnessTest extends BaseCardTest {

    @Test
    void twoOrMoreCreaturesAttackingOfferOnlyAttackingNonFliers() {
        Permanent landroval = addCreatureReady(player1, new LandrovalHorizonWitness());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent flier = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0, 1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId())
                .doesNotContain(landroval.getId(), flier.getId());
    }

    @Test
    void grantsFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new LandrovalHorizonWitness());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotTriggerWithOnlyOneAttacker() {
        Permanent landroval = addCreatureReady(player1, new LandrovalHorizonWitness());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, landroval, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotChooseAttackingCreatureWithFlying() {
        addCreatureReady(player1, new LandrovalHorizonWitness());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent flier = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, flier.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }
}
