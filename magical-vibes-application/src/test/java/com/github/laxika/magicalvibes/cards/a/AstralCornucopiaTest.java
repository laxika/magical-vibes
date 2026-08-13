package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AstralCornucopiaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X charge counters and adds the chosen color per counter")
    void entersWithXChargeCountersAndProducesChosenColor() {
        harness.setHand(player1, List.of(new AstralCornucopia()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent cornucopia = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cornucopia.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Produces no mana with no charge counters")
    void producesNoManaWithNoChargeCounters() {
        harness.addToBattlefield(player1, new AstralCornucopia());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
