package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NulltreadGargantuan.class, GrizzledLeotau.class})
class NulltreadGargantuanTest extends BaseCardTest {

    private void castNulltreadGargantuan() {
        harness.castFromHand(player1, new NulltreadGargantuan(), "{1}{G}{U}");
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("With no other creatures it must put itself on top of its owner's library")
    void topsItselfWithNoOtherCreatures() {
        castNulltreadGargantuan();
        harness.passBothPriorities(); // resolve ETB -> forced self-top

        harness.assertNotOnBattlefield(player1, "Nulltread Gargantuan");
        harness.assertNotInGraveyard(player1, "Nulltread Gargantuan");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo("Nulltread Gargantuan");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With another creature the controller is prompted to choose")
    void promptsWhenAnotherCreaturePresent() {
        harness.addToBattlefield(player1, new GrizzledLeotau());
        castNulltreadGargantuan();
        harness.passBothPriorities(); // resolve ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player1, "Nulltread Gargantuan");
    }

    @Test
    @DisplayName("Choosing another creature tops it and keeps Nulltread Gargantuan")
    void choosingAnotherCreatureTopsIt() {
        harness.addToBattlefield(player1, new GrizzledLeotau());
        castNulltreadGargantuan();
        harness.passBothPriorities();

        UUID leotauId = harness.getPermanentId(player1, "Grizzled Leotau");
        harness.handlePermanentChosen(player1, leotauId);

        harness.assertOnBattlefield(player1, "Nulltread Gargantuan");
        harness.assertNotOnBattlefield(player1, "Grizzled Leotau");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzled Leotau");
    }

    @Test
    @DisplayName("Controller may choose Nulltread Gargantuan itself even with another creature")
    void mayChooseItself() {
        harness.addToBattlefield(player1, new GrizzledLeotau());
        castNulltreadGargantuan();
        harness.passBothPriorities();

        UUID nulltreadId = harness.getPermanentId(player1, "Nulltread Gargantuan");
        harness.handlePermanentChosen(player1, nulltreadId);

        harness.assertNotOnBattlefield(player1, "Nulltread Gargantuan");
        harness.assertOnBattlefield(player1, "Grizzled Leotau");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Nulltread Gargantuan");
    }

    @Test
    @DisplayName("An opponent's creature is not a legal choice")
    void ignoresOpponentsCreatures() {
        harness.addToBattlefield(player2, new GrizzledLeotau());
        castNulltreadGargantuan();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nulltread Gargantuan");
        harness.assertOnBattlefield(player2, "Grizzled Leotau");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Nulltread Gargantuan");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A chosen creature goes to its owner's library rather than its controller's")
    void chosenCreatureGoesToOwnersLibrary() {
        GrizzledLeotau leotau = new GrizzledLeotau();
        leotau.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, leotau);
        harness.setLibrary(player1, List.of());
        castNulltreadGargantuan();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzled Leotau"));

        harness.assertOnBattlefield(player1, "Nulltread Gargantuan");
        harness.assertNotOnBattlefield(player1, "Grizzled Leotau");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(leotau);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger still puts the remaining creature on top after its source leaves")
    void remainingCreatureChosenAfterSourceLeaves() {
        GrizzledLeotau leotau = new GrizzledLeotau();
        harness.addToBattlefield(player1, leotau);
        castNulltreadGargantuan();
        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof NulltreadGargantuan).findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToLibraryTop(gd, source));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzled Leotau");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(leotau);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger resolves without a choice when its controller has no creatures left")
    void noCreaturesAtResolutionDoesNothing() {
        harness.addToBattlefield(player2, new GrizzledLeotau());
        castNulltreadGargantuan();
        var source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToLibraryTop(gd, source));
        List<Card> libraryBeforeResolution = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzled Leotau");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(libraryBeforeResolution);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
