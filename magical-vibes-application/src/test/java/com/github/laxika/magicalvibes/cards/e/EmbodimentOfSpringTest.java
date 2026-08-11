package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbodimentOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Embodiment of Spring sacrifices it and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        addReadyEmbodiment();
        addMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Embodiment of Spring");
        harness.assertInGraveyard(player1, "Embodiment of Spring");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving presents only basic lands with destination battlefield tapped")
    void resolvingPresentsOnlyBasicLandsToBattlefieldTapped() {
        activateSearch();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield tapped")
    void chosenBasicLandEntersTapped() {
        activateSearch();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyEmbodiment() {
        harness.addToBattlefield(player1, new EmbodimentOfSpring());
        Permanent embodiment = findPermanent(player1, "Embodiment of Spring");
        embodiment.setSummoningSick(false);
        return embodiment;
    }

    private void activateSearch() {
        addReadyEmbodiment();
        addMana();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
