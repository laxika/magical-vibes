package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnforgivingOne.class, FyndhornElves.class, GrizzlyBears.class})
class UnforgivingOneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns a target creature card within the number of modified creatures")
    void attackingReturnsCreatureWithinModifiedCreatureCount() {
        addCreatureReady(player1, new UnforgivingOne());
        Permanent modifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card eligible = new FyndhornElves();
        Card tooExpensive = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId());
    }

    @Test
    @DisplayName("An unmodified board provides no legal attack target")
    void noModifiedCreaturesMeansNoLegalTarget() {
        addCreatureReady(player1, new UnforgivingOne());
        Card eligibleWithoutModification = new FyndhornElves();
        harness.setGraveyard(player1, List.of(eligibleWithoutModification));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(eligibleWithoutModification);
    }

    @Test
    @DisplayName("A target becomes illegal if the modified-creature count decreases before resolution")
    void targetBecomesIllegalWhenModifiedCreatureCountDecreases() {
        addCreatureReady(player1, new UnforgivingOne());
        Permanent modifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card eligible = new FyndhornElves();
        harness.setGraveyard(player1, List.of(eligible));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
    }
}
