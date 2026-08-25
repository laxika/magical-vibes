package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfSurveillance.class, CurseOfThirst.class})
class CurseOfSurveillanceTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted player's upkeep lets the controller choose the other player")
    void choosesPlayersOtherThanTheEnchantedPlayer() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        harness.setLibrary(player1, List.of(new CurseOfThirst()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Each chosen player draws for every Curse attached to the enchanted player")
    void drawsForEachAttachedCurse() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        placeCurse(player1, player2, new CurseOfThirst());
        harness.setLibrary(player1, List.of(new CurseOfThirst(), new CurseOfThirst()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("The controller may choose no target players")
    void mayChooseNoPlayers() {
        placeCurse(player1, player2, new CurseOfSurveillance());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    private Permanent placeCurse(Player controller, Player enchantedPlayer, com.github.laxika.magicalvibes.model.Card curse) {
        Permanent permanent = new Permanent(curse);
        permanent.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }
}
