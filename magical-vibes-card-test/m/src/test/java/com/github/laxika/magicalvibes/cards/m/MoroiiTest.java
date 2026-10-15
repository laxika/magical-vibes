package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Moroii.class)
class MoroiiTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger makes its controller lose 1 life")
    void upkeepMakesControllerLoseLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Moroii());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Moroii creates a separate upkeep trigger")
    void eachMoroiiTriggersSeparately() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Moroii());
        addCreatureReady(player1, new Moroii());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Moroii());

        advanceToUpkeep(player2);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep life loss affects the controller rather than the owner")
    void upkeepMakesControllerRatherThanOwnerLoseLife() {
        Moroii moroii = new Moroii();
        moroii.setOwnerId(player1.getId());
        addCreatureReady(player2, moroii);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Upkeep trigger still resolves after Moroii dies")
    void upkeepTriggerResolvesAfterSourceDies() {
        var moroii = addCreatureReady(player1, new Moroii());
        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        moroii.setToughnessModifier(-4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(moroii.getCard());
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
