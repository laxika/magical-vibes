package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DriftOfPhantasms.class, Convolute.class, DizzySpell.class, DrakeFamiliar.class})
class DriftOfPhantasmsTest extends BaseCardTest {

    @Test
    void transmuteSearchesForTheSameManaValue() {
        Convolute matchingCard = new Convolute();
        DizzySpell lowerManaValue = new DizzySpell();
        DrakeFamiliar lowerManaValueToo = new DrakeFamiliar();
        harness.setHand(player1, List.of(new DriftOfPhantasms()));
        harness.setLibrary(player1, List.of(matchingCard, lowerManaValue, lowerManaValueToo));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Drift of Phantasms");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedDuringYourMainPhase() {
        DriftOfPhantasms drift = new DriftOfPhantasms();
        harness.setHand(player1, List.of(drift));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed during your main phase");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drift);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void transmutePaysManaAndDiscardsBeforeResolving() {
        DriftOfPhantasms drift = new DriftOfPhantasms();
        Convolute matchingCard = new Convolute();
        harness.setHand(player1, List.of(drift));
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drift);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteCanFailToFindEvenWhenAMatchingCardExists() {
        Convolute matchingCard = new Convolute();
        harness.setHand(player1, List.of(new DriftOfPhantasms()));
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        harness.assertInGraveyard(player1, "Drift of Phantasms");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transmuteRequiresTwoBlueMana() {
        DriftOfPhantasms drift = new DriftOfPhantasms();
        harness.setHand(player1, List.of(drift));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drift);
        harness.assertNotInGraveyard(player1, "Drift of Phantasms");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transmuteCannotBeActivatedWhileAnotherAbilityIsOnTheStack() {
        DriftOfPhantasms secondDrift = new DriftOfPhantasms();
        harness.setHand(player1, List.of(new DriftOfPhantasms(), secondDrift));
        harness.setLibrary(player1, List.of(new Convolute()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDrift);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void transmuteResolvesWithoutFindingACardWhenNoManaValueMatches() {
        DizzySpell nonmatchingCard = new DizzySpell();
        harness.setHand(player1, List.of(new DriftOfPhantasms()));
        harness.setLibrary(player1, List.of(nonmatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
        harness.assertInGraveyard(player1, "Drift of Phantasms");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transmuteCannotBeActivatedDuringOpponentsMainPhase() {
        DriftOfPhantasms drift = new DriftOfPhantasms();
        harness.setHand(player1, List.of(drift));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drift);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
