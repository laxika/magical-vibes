package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MinamoScrollkeeper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LearnedLearner.class, MinamoScrollkeeper.class, GrizzlyBears.class})
class LearnedLearnerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have the draw ability with a maximum hand size of seven")
    void noDrawAbilityWithMaximumHandSizeSeven() {
        addCreatureReady(player1, new LearnedLearner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Has the draw ability when the controller's maximum hand size is not seven")
    void drawsWithMaximumHandSizeOtherThanSeven() {
        Permanent learner = addCreatureReady(player1, new LearnedLearner());
        harness.addToBattlefield(player1, new MinamoScrollkeeper());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int learnerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(learner);

        harness.activateAbility(player1, learnerIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1)
                .contains(drawnCard);
    }
}
