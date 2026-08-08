package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlastOfGeniusTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Draws three cards, then the discarded card's mana value is dealt to the targeted player")
    void dealsDiscardedManaValueToPlayer() {
        harness.setHand(player1, List.of(new BlastOfGenius()));
        addMana(player1);
        // Grizzly Bears ({1}{G}, mana value 2) is drawn and discarded.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage equals the discarded card's mana value, not a fixed amount")
    void damageScalesWithDiscardedManaValue() {
        harness.setHand(player1, List.of(new BlastOfGenius()));
        addMana(player1);
        // Wind Drake ({2}{U}, mana value 3) is drawn and discarded.
        harness.setLibrary(player1, List.of(new WindDrake(), new WindDrake(), new WindDrake()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target a creature — lethal discarded mana value destroys it")
    void destroysTargetedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new BlastOfGenius()));
        addMana(player1);
        // Wind Drake's mana value 3 is lethal to a 2/2.
        harness.setLibrary(player1, List.of(new WindDrake(), new WindDrake(), new WindDrake()));

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
