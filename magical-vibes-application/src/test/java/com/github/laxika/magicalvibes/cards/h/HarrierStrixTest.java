package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarrierStrix.class, Forest.class, GrizzlyBears.class, Island.class})
class HarrierStrixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target permanent")
    void etbTapsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new HarrierStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability draws a card, then prompts for a discard")
    void activatedAbilityDrawsThenDiscards() {
        addReadyHarrierStrix(player1);
        Card discarded = new GrizzlyBears();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        setDeck(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    private Permanent addReadyHarrierStrix(Player player) {
        Permanent harrier = new Permanent(new HarrierStrix());
        harrier.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(harrier);
        return harrier;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
