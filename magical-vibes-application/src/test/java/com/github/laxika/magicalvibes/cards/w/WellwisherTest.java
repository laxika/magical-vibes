package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wellwisher.class, LlanowarElves.class, RagingGoblin.class, Tarfire.class})
class WellwisherTest extends BaseCardTest {

    @Test
    void gainsLifeForEachElfOnBothBattlefieldsAtResolution() {
        Permanent wellwisher = addCreatureReady(player1, new Wellwisher());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.passBothPriorities();

        assertThat(wellwisher.isTapped()).isTrue();
        harness.assertLife(player1, 13);
    }

    @Test
    void abilityStillResolvesAfterSourceDiesAndCountsOnlyRemainingElves() {
        Permanent wellwisher = addCreatureReady(player1, new Wellwisher());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, wellwisher.getId());
        harness.assertInGraveyard(player1, "Wellwisher");
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 15);
    }

    @Test
    void gainsNoLifeWhenNoElvesRemainAtResolution() {
        Permanent wellwisher = addCreatureReady(player1, new Wellwisher());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, wellwisher.getId());
        harness.assertInGraveyard(player1, "Wellwisher");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Wellwisher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new Wellwisher());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }
}
