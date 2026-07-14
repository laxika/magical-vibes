package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrumblingAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger destroys the targeted creature with a -1/-1 counter")
    void destroysCreatureWithCounter() {
        addReady(player1, new CrumblingAshes());
        Permanent bears = addReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not trigger when no creature has a -1/-1 counter")
    void doesNotTriggerWithoutCounter() {
        addReady(player1, new CrumblingAshes());
        addReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Creature without a -1/-1 counter is not a legal target")
    void creatureWithoutCounterNotTargetable() {
        addReady(player1, new CrumblingAshes());
        Permanent withCounter = addReady(player2, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent withoutCounter = addReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        // The creature without a -1/-1 counter is not a legal target and is rejected
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, withoutCounter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
