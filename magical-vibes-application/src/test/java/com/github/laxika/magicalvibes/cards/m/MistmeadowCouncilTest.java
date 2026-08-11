package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MistmeadowCouncilTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an ETB draw trigger")
    void entersWithDrawTrigger() {
        setDeck(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistmeadow Council");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Costs the full amount without a Kithkin")
    void costsFullAmountWithoutKithkin() {
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs one less while controlling a Kithkin")
    void costsOneLessWithKithkin() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        harness.setHand(player1, List.of(new MistmeadowCouncil()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
