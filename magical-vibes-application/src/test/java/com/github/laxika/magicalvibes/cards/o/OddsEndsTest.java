package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OddsEnds.class, Divination.class, EdgarKingOfFigaro.class, Forest.class, GrizzlyBears.class})
class OddsEndsTest extends BaseCardTest {

    @Test
    @DisplayName("Odds counters a targeted instant or sorcery when the flip comes up heads")
    void oddsCountersTargetedSpellOnHeads() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, divination, "{2}{U}");

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, 0, divination.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Odds copies the spell when the flip comes up tails")
    void oddsCopiesTargetedSpellOnTails() {
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, divination, "{2}{U}");

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, 0, divination.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        if (gameLogContains("wins the coin flip for Odds")) {
            harness.assertInGraveyard(player2, "Divination");
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        } else {
            resolveAllTriggers();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        }
    }

    @Test
    @DisplayName("Odds cannot target a player")
    void oddsCannotTargetPlayer() {
        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ends sacrifices two attacking creatures chosen by the targeted player")
    void endsSacrificesTwoAttackingCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new OddsEnds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        declareAttackers(player2, List.of(0, 1, 2));
        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getId())
                .containsExactly(third.getId());
    }
}
