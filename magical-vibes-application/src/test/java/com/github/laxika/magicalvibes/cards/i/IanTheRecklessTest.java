package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IanTheReckless.class)
class IanTheRecklessTest extends BaseCardTest {

    @Test
    @DisplayName("A modified Ian may deal its power to you and any target")
    void modifiedIanDealsPowerDamageToBothTargets() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Declining Ian's attack trigger deals no damage")
    void decliningTriggerDealsNoDamage() {
        Permanent ian = addCreatureReady(player1, new IanTheReckless());
        ian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An unmodified Ian does not create an attack trigger")
    void unmodifiedIanDoesNotTrigger() {
        addCreatureReady(player1, new IanTheReckless());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
