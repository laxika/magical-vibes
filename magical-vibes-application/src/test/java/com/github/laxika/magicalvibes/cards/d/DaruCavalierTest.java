package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaruCavalier.class, GrizzlyBears.class})
class DaruCavalierTest extends BaseCardTest {

    @Test
    void acceptingMaySearchesForOneDaruCavalier() {
        castDaruCavalier();
        harness.setLibrary(player1, List.of(new DaruCavalier(), new GrizzlyBears()));

        resolveEnterTheBattlefieldAbility();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Daru Cavalier");

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Daru Cavalier");
    }

    @Test
    void decliningMayDoesNotSearch() {
        castDaruCavalier();
        harness.setLibrary(player1, List.of(new DaruCavalier(), new GrizzlyBears()));

        resolveEnterTheBattlefieldAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Daru Cavalier");
    }

    @Test
    void noMatchingCardDoesNotCreateSearchPrompt() {
        castDaruCavalier();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveEnterTheBattlefieldAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Daru Cavalier");
    }

    private void castDaruCavalier() {
        harness.setHand(player1, List.of(new DaruCavalier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldAbility() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
