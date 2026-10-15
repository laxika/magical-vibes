package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.FlowOfIdeas;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NetherbornPhalanx.class, FlowOfIdeas.class, BorosRecruit.class, Watchwolf.class})
class NetherbornPhalanxTest extends BaseCardTest {

    @Test
    void eachOpponentLosesLifeForEachCreatureTheyControl() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.addToBattlefield(player2, new Watchwolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new NetherbornPhalanx(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        NetherbornPhalanx phalanx = new NetherbornPhalanx();
        FlowOfIdeas matchingCard = new FlowOfIdeas();
        BorosRecruit lowerManaValue = new BorosRecruit();
        Watchwolf differentManaValue = new Watchwolf();
        harness.setHand(player1, List.of(phalanx));
        harness.setLibrary(player1, List.of(matchingCard, lowerManaValue, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Netherborn Phalanx");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteShufflesWithoutFindingAMatchingCard() {
        NetherbornPhalanx phalanx = new NetherbornPhalanx();
        BorosRecruit nonMatchingCard = new BorosRecruit();
        harness.setHand(player1, List.of(phalanx));
        harness.setLibrary(player1, List.of(nonMatchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Netherborn Phalanx");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        NetherbornPhalanx phalanx = new NetherbornPhalanx();
        harness.setHand(player1, List.of(phalanx));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(phalanx);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void opponentWithNoCreaturesLosesNoLife() {
        harness.addToBattlefield(player1, new Watchwolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new NetherbornPhalanx(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void countsCreaturesWhenTheEnterTriggerResolves() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new NetherbornPhalanx(), "{5}{B}");
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new Watchwolf());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void transmuteMayFailToFindEvenWithAMatchingCard() {
        FlowOfIdeas matchingCard = new FlowOfIdeas();
        harness.setHand(player1, List.of(new NetherbornPhalanx()));
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Netherborn Phalanx");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void transmuteCannotBeActivatedWhileTheStackIsNotEmpty() {
        harness.castFromHand(player1, new NetherbornPhalanx(), "{5}{B}");
        NetherbornPhalanx phalanx = new NetherbornPhalanx();
        harness.setHand(player1, List.of(phalanx));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(phalanx);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
