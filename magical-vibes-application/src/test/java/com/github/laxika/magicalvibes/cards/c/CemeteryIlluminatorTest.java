package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CemeteryIlluminator.class, GrizzlyBears.class, Shock.class})
class CemeteryIlluminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles and remembers a graveyard card")
    void exilesAndImprintsOnEnter() {
        Card exiled = new GrizzlyBears();
        Permanent illuminator = enterIlluminatorWith(exiled);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
        assertThat(gd.getImprintedCard(illuminator.getCard())).isSameAs(exiled);
    }

    @Test
    @DisplayName("Attacking exiles and remembers another graveyard card")
    void exilesAndImprintsOnAttack() {
        Card firstExiled = new GrizzlyBears();
        Permanent illuminator = enterIlluminatorWith(firstExiled);
        illuminator.setSummoningSick(false);

        Card secondExiled = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(secondExiled));
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(secondExiled.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstExiled, secondExiled);
        assertThat(gd.getImprintedCard(illuminator.getCard())).isSameAs(secondExiled);
    }

    @Test
    @DisplayName("Casts one matching spell from the top of the library each turn")
    void castsMatchingTopSpellOnlyOnceEachTurn() {
        enterIlluminatorWith(new GrizzlyBears());
        Card firstSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Card secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(secondSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondSpell);
    }

    @Test
    @DisplayName("Cannot cast a top spell without a shared card type")
    void rejectsNonMatchingTopSpell() {
        enterIlluminatorWith(new GrizzlyBears());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    private Permanent enterIlluminatorWith(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent illuminator = harness.enterBattlefieldAndReturn(player1, new CemeteryIlluminator());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        return illuminator;
    }
}
