package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LotusCobraTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall adds one mana of the chosen color")
    void landfallAddsMana() {
        harness.addToBattlefield(player1, new LotusCobra());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's landfall does not add mana")
    void opponentLandfallDoesNotAddMana() {
        harness.addToBattlefield(player1, new LotusCobra());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
