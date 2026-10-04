package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.c.ChanceEncounter;
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

@CardUsed({GoblinAssassin.class, FugitiveWizard.class, GoblinGrappler.class, ChanceEncounter.class})
class GoblinAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry makes each player flip and tails players sacrifice a creature")
    void eachPlayerFlipsAndTailsPlayersSacrifice() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new GoblinGrappler());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new GoblinGrappler());

        harness.setHand(player1, List.of(new GoblinAssassin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        resolveSacrificeChoices();

        List<String> flipLogs = flipLogs();
        long tails = flipLogs.stream().filter(log -> log.contains(" loses the coin flip ")).count();
        long sacrificedSupportingCreatures = countInGraveyard(player1, "Fugitive Wizard")
                + countInGraveyard(player1, "Goblin Grappler")
                + countInGraveyard(player2, "Fugitive Wizard")
                + countInGraveyard(player2, "Goblin Grappler");

        assertThat(flipLogs).hasSize(2);
        assertThat(sacrificedSupportingCreatures).isEqualTo(tails);
        assertThat(countPermanents(player1, "Goblin Assassin")).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for an opponent's Goblin but not for a non-Goblin creature")
    void triggersForGoblinEntriesOnly() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new GoblinGrappler());
        harness.addToBattlefield(player1, new GoblinAssassin());
        harness.setHand(player2, List.of(new GoblinGrappler(), new FugitiveWizard()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        resolveSacrificeChoices();
        assertThat(flipLogs()).hasSize(2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(flipLogs()).hasSize(2);
        assertThat(countPermanents(player1, "Goblin Assassin")).isEqualTo(1);
    }

    @Test
    @DisplayName("The only creature can sacrifice itself and a creatureless player still flips")
    void assassinCanSacrificeItselfWithCreaturelessOpponent() {
        harness.setHand(player1, List.of(new GoblinAssassin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(flipLogs()).hasSize(2);
        boolean controllerTails = flipLogs().stream().anyMatch(log ->
                log.startsWith(gd.playerIdToName.get(player1.getId()) + " loses the coin flip "));
        assertThat(countInGraveyard(player1, "Goblin Assassin")).isEqualTo(controllerTails ? 1 : 0);
        assertThat(countPermanents(player1, "Goblin Assassin")).isEqualTo(controllerTails ? 0 : 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Heads or tails flips do not trigger Chance Encounter")
    void flipsHaveNoWinner() {
        harness.addToBattlefield(player1, new ChanceEncounter());
        harness.addToBattlefield(player2, new ChanceEncounter());
        harness.setHand(player1, List.of(new GoblinAssassin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flipLogs()).hasSize(2);
        assertThat(gd.stack).isEmpty();
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
