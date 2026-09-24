package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishFaithbonder.class, GrizzlyBears.class})
class BenalishFaithbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Benalish Faithbonder untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());

        declareAttackers(List.of(0));

        assertThat(faithbonder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and gives Benalish Faithbonder its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(supporter.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(faithbonder.getPowerModifier()).isEqualTo(2);
        assertThat(faithbonder.getToughnessModifier()).isZero();
    }
}
