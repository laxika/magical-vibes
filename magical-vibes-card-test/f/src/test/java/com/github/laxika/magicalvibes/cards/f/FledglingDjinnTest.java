package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FledglingDjinn.class)
class FledglingDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to its controller")
    void upkeepDamagesController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new FledglingDjinn());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new FledglingDjinn());

        advanceToUpkeep(player2);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Fledgling Djinn triggers separately during its controller's upkeep")
    void eachCopyTriggersSeparately() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new FledglingDjinn());
        addCreatureReady(player1, new FledglingDjinn());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A Djinn controlled by the second player damages only that player")
    void upkeepDamagesSecondPlayerController() {
        addCreatureReady(player1, new FledglingDjinn());
        addCreatureReady(player2, new FledglingDjinn());

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Upkeep damage still resolves after the Djinn leaves the battlefield")
    void upkeepDamageResolvesAfterSourceLeaves() {
        var djinn = addCreatureReady(player1, new FledglingDjinn());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, djinn);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Fledgling Djinn");
    }
}
