package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NevermakerTest extends BaseCardTest {

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB tucks target nonland permanent on top of its owner's library")
    void evokeTucksTargetOnEntrySacrifice() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new Nevermaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB (evoke sacrifice) -> LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB trigger

        // Nevermaker sacrificed as it entered.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nevermaker"));
        // Target tucked on top of its owner's library.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Leaves the battlefield (non-evoke) =====

    @Test
    @DisplayName("LTB fires on any leave and tucks the chosen nonland permanent on top of its owner's library")
    void leavesBattlefieldTucksTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> target prompt

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve LTB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("LTB has no valid target when only a land is available (nonland restriction)")
    void leavesBattlefieldSkipsWhenOnlyLandAvailable() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent nevermaker = harness.addToBattlefieldAndReturn(player1, new Nevermaker());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nevermaker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger has no valid targets -> skipped

        // Land untouched, nothing tucked.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }
}
