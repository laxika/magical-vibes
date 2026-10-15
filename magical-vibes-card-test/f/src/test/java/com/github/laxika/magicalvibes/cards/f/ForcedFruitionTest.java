package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForcedFruition.class, GoldmeadowHarrier.class, Island.class, WitchbaneOrb.class})
class ForcedFruitionTest extends BaseCardTest {

    private List<com.github.laxika.magicalvibes.model.Card> tenCardLibrary() {
        var cards = new ArrayList<com.github.laxika.magicalvibes.model.Card>();
        for (int i = 0; i < 10; i++) {
            cards.add(new GoldmeadowHarrier());
        }
        return cards;
    }

    @Test
    @DisplayName("Opponent casting a spell puts triggered ability on stack")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("The casting opponent draws seven cards")
    void castingOpponentDrawsSeven() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}"); // hand -> empty after cast
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 7);
    }

    @Test
    @DisplayName("The controller of Forced Fruition does not draw")
    void controllerDoesNotDraw() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());
        harness.setHand(player1, List.of());

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.castFromHand(player1, new GoldmeadowHarrier(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Does NOT trigger when an opponent plays a land")
    void doesNotTriggerOnOpponentLandPlay() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Island()));
        harness.playLand(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each copy triggers separately and draws seven cards before the spell resolves")
    void multipleCopiesDrawFourteen() {
        harness.addToBattlefield(player1, new ForcedFruition());
        harness.addToBattlefield(player1, new ForcedFruition());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var library = new ArrayList<>(tenCardLibrary());
        library.addAll(tenCardLibrary());
        harness.setLibrary(player2, library);

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(14);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Goldmeadow Harrier");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Goldmeadow Harrier");
    }

    @Test
    @DisplayName("An opponent's noncreature spell triggers only the existing Forced Fruition")
    void triggersOnOpponentEnchantmentSpell() {
        harness.addToBattlefield(player1, new ForcedFruition());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new ForcedFruition(), "{4}{U}{U}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        harness.assertNotOnBattlefield(player2, "Forced Fruition");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Forced Fruition");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending draw trigger survives its source leaving the battlefield")
    void pendingTriggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new ForcedFruition());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, tenCardLibrary());
        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");

        var source = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @CardUsed(WitchbaneOrb.class)
    @DisplayName("An opponent with hexproof still draws because the ability does not target")
    void hexproofDoesNotPreventOpponentDrawing() {
        harness.addToBattlefield(player1, new ForcedFruition());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
}
