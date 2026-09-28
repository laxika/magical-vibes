package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraconicMuralists.class, Terminate.class, DragonHatchling.class, GrizzlyBears.class})
class DraconicMuralistsTest extends BaseCardTest {

    @Test
    @DisplayName("When Draconic Muralists dies, its controller may search for a Dragon")
    void deathCreatesMayPrompt() {
        killMuralists();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the search offers only Dragon cards and puts the chosen card into hand")
    void acceptingSearchFindsDragon() {
        harness.setLibrary(player1, List.of(new DragonHatchling(), new GrizzlyBears()));
        killMuralists();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement().extracting(Card::getName).isEqualTo("Dragon Hatchling");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Dragon Hatchling");
    }

    @Test
    @DisplayName("Declining the search does not search the library")
    void decliningSearchSkipsSearch() {
        harness.setLibrary(player1, List.of(new DragonHatchling()));
        killMuralists();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("A library without a Dragon completes the search without finding a card")
    void searchWithNoDragonCompletes() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        killMuralists();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("Dragon Hatchling"));
    }

    private void killMuralists() {
        harness.addToBattlefield(player1, new DraconicMuralists());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Draconic Muralists"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
