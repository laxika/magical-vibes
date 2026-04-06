package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class GameDataDeepCopyTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
    }

    @Test
    @DisplayName("Deep copy preserves primitive fields")
    void deepCopyPreservesPrimitives() {
        gd.turnNumber = 5;
        gd.currentStep = TurnStep.POSTCOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.playerLifeTotals.put(player1.getId(), 15);
        gd.playerLifeTotals.put(player2.getId(), 8);

        GameData copy = gd.simulationCopy();

        assertThat(copy.turnNumber).isEqualTo(5);
        assertThat(copy.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(copy.activePlayerId).isEqualTo(player1.getId());
        assertThat(copy.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(copy.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Deep copy creates independent battlefield collections")
    void deepCopyIndependentBattlefields() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GameData copy = gd.simulationCopy();

        // Verify the copy has the creature
        assertThat(copy.playerBattlefields.get(player1.getId())).hasSize(
                gd.playerBattlefields.get(player1.getId()).size());

        // Modify the copy — original should be unaffected
        copy.playerBattlefields.get(player1.getId()).clear();
        assertThat(gd.playerBattlefields.get(player1.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Deep copy creates independent Permanent objects")
    void deepCopyIndependentPermanents() {
        harness.addToBattlefield(player1, new SerraAngel());

        GameData copy = gd.simulationCopy();

        // Modify the copy's permanent
        Permanent copyPerm = copy.playerBattlefields.get(player1.getId()).getFirst();
        copyPerm.tap();

        // Original should be unaffected
        Permanent origPerm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(origPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Deep copy preserves Card reference sharing")
    void deepCopySharesCardReferences() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GameData copy = gd.simulationCopy();

        Card origCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card copyCard = copy.playerBattlefields.get(player1.getId()).getFirst().getCard();

        // Same Card object (not deep copied — immutable)
        assertThat(copyCard).isSameAs(origCard);
    }

    @Test
    @DisplayName("Deep copy preserves Permanent IDs (references stay valid)")
    void deepCopyPreservesPermanentIds() {
        harness.addToBattlefield(player1, new SerraAngel());

        GameData copy = gd.simulationCopy();

        Permanent origPerm = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent copyPerm = copy.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(copyPerm.getId()).isEqualTo(origPerm.getId());
    }

    @Test
    @DisplayName("Deep copy creates independent hand collections")
    void deepCopyIndependentHands() {
        GameData copy = gd.simulationCopy();

        int origHandSize = gd.playerHands.get(player1.getId()).size();
        copy.playerHands.get(player1.getId()).clear();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(origHandSize);
    }

    @Test
    @DisplayName("Deep copy preserves mana pool values independently")
    void deepCopyIndependentManaPools() {
        harness.addMana(player1, ManaColor.RED, 3);

        GameData copy = gd.simulationCopy();

        assertThat(copy.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        // Modify copy
        copy.playerManaPools.get(player1.getId()).add(ManaColor.RED);

        // Original unchanged
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deep copy preserves life totals independently")
    void deepCopyIndependentLifeTotals() {
        gd.playerLifeTotals.put(player1.getId(), 12);

        GameData copy = gd.simulationCopy();
        copy.playerLifeTotals.put(player1.getId(), 5);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Deep copy preserves player ordering")
    void deepCopyPreservesPlayerOrder() {
        GameData copy = gd.simulationCopy();

        assertThat(copy.orderedPlayerIds).containsExactlyElementsOf(gd.orderedPlayerIds);
        assertThat(copy.playerIds).containsExactlyInAnyOrderElementsOf(gd.playerIds);
    }

    @Test
    @DisplayName("Deep copy preserves stack entries independently")
    void deepCopyIndependentStack() {
        // The stack is empty after setup, verify copy is also empty
        GameData copy = gd.simulationCopy();
        assertThat(copy.stack).isEmpty();
        assertThat(copy.stack).isNotSameAs(gd.stack);
    }
}
