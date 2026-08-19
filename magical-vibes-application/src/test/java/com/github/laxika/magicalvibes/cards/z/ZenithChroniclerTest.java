package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZenithChroniclerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws when they cast their first multicolored spell")
    void triggersForEachPlayerIndependently() {
        harness.addToBattlefield(player1, new ZenithChronicler());
        harness.setHand(player1, List.of(new WoollyThoctar(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new WoollyThoctar()));
        addWoollyMana(player1);
        addWoollyMana(player2);

        int player1HandBeforeCast = gd.playerHands.get(player1.getId()).size();
        int player2HandBeforeCast = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBeforeCast + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBeforeCast - 1);

        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBeforeCast);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBeforeCast);
    }

    @Test
    @DisplayName("Triggers only for the first multicolored spell each turn")
    void ignoresMonocoloredAndLaterMulticoloredSpells() {
        harness.addToBattlefield(player1, new ZenithChronicler());
        harness.setHand(player1, List.of(new GrizzlyBears(), new WoollyThoctar(), new WoollyThoctar()));
        addMana(player1, ManaColor.GREEN, 1);
        addMana(player1, ManaColor.COLORLESS, 1);
        addWoollyMana(player1);
        addWoollyMana(player1);

        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);

        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);
    }

    private void addWoollyMana(Player player) {
        addMana(player, ManaColor.RED, 1);
        addMana(player, ManaColor.GREEN, 1);
        addMana(player, ManaColor.WHITE, 1);
    }

    private void addMana(Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }
}
