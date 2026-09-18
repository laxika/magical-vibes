package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalitionWarbrute.class, GrizzlyBears.class})
class CoalitionWarbruteTest extends BaseCardTest {

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Coalition Warbrute by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(warbrute.getPowerModifier()).isEqualTo(2);
        assertThat(warbrute.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist cannot use a creature with summoning sickness")
    void enlistExcludesSummoningSickCreature() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent summoningSick = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(summoningSick);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(warbrute.getPowerModifier()).isZero();
    }
}
