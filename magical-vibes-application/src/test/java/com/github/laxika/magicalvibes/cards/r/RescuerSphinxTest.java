package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RescuerSphinx.class, GrizzlyBears.class, Island.class})
class RescuerSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return another nonland permanent and put a counter on Rescuer Sphinx")
    void returnsPermanentAndEntersWithCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RescuerSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ETB choice leaves permanents unchanged")
    void decliningLeavesPermanentsUnchanged() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RescuerSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lands are not legal choices")
    void landCannotBeReturned() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new RescuerSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Island");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
