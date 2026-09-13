package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingBrontodon.class, Forest.class, Island.class})
class RampagingBrontodonTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each land you control when it attacks")
    void boostsForControlledLands() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(9);
    }

    @Test
    @DisplayName("Does not count lands controlled by an opponent")
    void countsOnlyControlledLands() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(7);
    }

    @Test
    @DisplayName("The attack boost lasts until end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent brontodon = addCreatureReady(player1, new RampagingBrontodon());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(7);
    }
}
