package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinAssassin.class, GrizzlyBears.class, HillGiant.class, RagingGoblin.class})
class GoblinAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry makes each player flip and tails players sacrifice a creature")
    void eachPlayerFlipsAndTailsPlayersSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player1, List.of(new GoblinAssassin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveSacrificeChoices();

        List<String> flipLogs = flipLogs();
        long tails = flipLogs.stream().filter(log -> log.contains(" loses the coin flip ")).count();
        long sacrificedSupportingCreatures = countInGraveyard(player1, "Grizzly Bears")
                + countInGraveyard(player1, "Hill Giant")
                + countInGraveyard(player2, "Grizzly Bears")
                + countInGraveyard(player2, "Hill Giant");

        assertThat(flipLogs).hasSize(2);
        assertThat(sacrificedSupportingCreatures).isEqualTo(tails);
        assertThat(countPermanents(player1, "Goblin Assassin")).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for an opponent's Goblin but not for a non-Goblin creature")
    void triggersForGoblinEntriesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GoblinAssassin());
        harness.setHand(player2, List.of(new RagingGoblin(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveSacrificeChoices();
        assertThat(flipLogs()).hasSize(2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(flipLogs()).hasSize(2);
        assertThat(countPermanents(player1, "Goblin Assassin")).isEqualTo(1);
    }

    private void resolveSacrificeChoices() {
        PendingInteraction.MultiPermanentChoice choice;
        while ((choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)) != null) {
            harness.handleMultiplePermanentsChosen(playerFor(choice.playerId()),
                    List.of(choice.validIds().getFirst()));
        }
    }

    private Player playerFor(java.util.UUID playerId) {
        return player1.getId().equals(playerId) ? player1 : player2;
    }

    private List<String> flipLogs() {
        return gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Goblin Assassin"))
                .toList();
    }

    private long countInGraveyard(Player player, String cardName) {
        return gd.playerGraveyards.get(player.getId()).stream()
                .filter(card -> card.getName().equals(cardName))
                .count();
    }
}
