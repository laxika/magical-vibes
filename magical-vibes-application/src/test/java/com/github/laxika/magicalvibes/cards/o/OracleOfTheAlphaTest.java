package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Deglamer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OracleOfTheAlpha.class, Deglamer.class})
class OracleOfTheAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB conjures one of each Power Nine card into the library")
    void etbConjuresPowerNine() {
        harness.setHand(player1, List.of(new OracleOfTheAlpha()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(9);
        assertThat(library).extracting(Card::getName).containsExactlyInAnyOrder(
                "Black Lotus", "Mox Pearl", "Mox Sapphire", "Mox Ruby", "Mox Jet", "Mox Emerald",
                "Ancestral Recall", "Time Walk", "Timetwister");
    }

    @Test
    @DisplayName("Attacking starts a scry 1 interaction")
    void attackScriesOne() {
        Card top = new OracleOfTheAlpha();
        Card bottom = new OracleOfTheAlpha();
        harness.setLibrary(player1, List.of(top, bottom));
        addCreatureReady(player1, new OracleOfTheAlpha());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Each entry adds another Power Nine and preserves existing cards")
    void repeatedEntriesConjureFreshPowerNine() {
        Card existing = new OracleOfTheAlpha();
        harness.setLibrary(player2, List.of(existing));
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player2, new OracleOfTheAlpha());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new OracleOfTheAlpha());
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(19).contains(existing);
        assertThat(library).extracting(Card::getId).doesNotHaveDuplicates();
        assertThat(library.stream().filter(card -> card != existing).map(Card::getName).toList())
                .containsExactlyInAnyOrder(
                        "Black Lotus", "Mox Pearl", "Mox Sapphire", "Mox Ruby", "Mox Jet", "Mox Emerald",
                        "Ancestral Recall", "Time Walk", "Timetwister",
                        "Black Lotus", "Mox Pearl", "Mox Sapphire", "Mox Ruby", "Mox Jet", "Mox Emerald",
                        "Ancestral Recall", "Time Walk", "Timetwister");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller can keep the scried card on top")
    void opponentAttackCanKeepTopCard() {
        Card top = new OracleOfTheAlpha();
        Card bottom = new OracleOfTheAlpha();
        harness.setLibrary(player2, List.of(top, bottom));
        addCreatureReady(player2, new OracleOfTheAlpha());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, bottom);
    }

    @Test
    @DisplayName("Attacking with an empty library completes without a scry prompt")
    void emptyLibraryScryCompletes() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new OracleOfTheAlpha());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A conjured Mox can be shuffled back into its owner's library")
    void conjuredMoxCanReturnToLibrary() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new OracleOfTheAlpha());
        resolveAllTriggers();
        Card mox = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Mox Sapphire"))
                .findFirst().orElseThrow();
        gd.playerDecks.get(player1.getId()).remove(mox);
        harness.setHand(player1, List.of(mox));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Deglamer()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Mox Sapphire"));

        harness.assertNotOnBattlefield(player1, "Mox Sapphire");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(9).contains(mox);
    }
}
