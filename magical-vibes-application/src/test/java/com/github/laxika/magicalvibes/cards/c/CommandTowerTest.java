package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.e.ExoticOrchard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandTower.class, BalefulStrix.class, EdgarMarkov.class, ExoticOrchard.class})
class CommandTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Produces mana only from the commander's color identity")
    void producesManaInCommandersColorIdentity() {
        gd.playerCommanders.put(player1.getId(), java.util.List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new CommandTower());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLACK", "RED");

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a color outside the commander's color identity")
    void rejectsColorOutsideCommandersIdentity() {
        gd.playerCommanders.put(player1.getId(), java.util.List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new CommandTower());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.GREEN.name()))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, ManaColor.WHITE.name());
    }

    @Test
    @DisplayName("Other lands see only the commander's color identity")
    void otherLandsSeeOnlyCommandersIdentity() {
        gd.playerCommanders.put(player2.getId(), java.util.List.of(new EdgarMarkov()));
        harness.addToBattlefield(player2, new CommandTower());
        harness.addToBattlefield(player1, new ExoticOrchard());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK", "RED");
    }
}
