package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SidewinderSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DementiaSliver.class, SidewinderSliver.class, DurkwoodBaloth.class})
class DementiaSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A matching revealed card is discarded")
    void matchingRevealedCardIsDiscarded() {
        addReadyDementiaSliver(player1);
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Durkwood Baloth");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("A nonmatching revealed card remains in its owner's hand")
    void nonmatchingRevealedCardIsNotDiscarded() {
        addReadyDementiaSliver(player1);
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dementia Sliver");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability is granted to another Sliver")
    void grantsAbilityToAnotherSliver() {
        harness.addToBattlefield(player1, new DementiaSliver());
        addReadySliver(player1);
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Durkwood Baloth");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The ability is granted to an opposing Sliver")
    void grantsAbilityToOpposingSliver() {
        harness.addToBattlefield(player1, new DementiaSliver());
        addReadySliver(player2);
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player1, List.of(card));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Durkwood Baloth");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The ability can target only an opponent and can activate only during its controller's turn")
    void enforcesTargetAndTimingRestrictions() {
        addReadyDementiaSliver(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("The ability is not granted to non-Sliver creatures")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new DementiaSliver());
        harness.addToBattlefield(player1, new DurkwoodBaloth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Exactly one matching card is discarded from a hand with multiple copies")
    void discardsOnlyOneRandomlyRevealedCard() {
        addReadyDementiaSliver(player1);
        DurkwoodBaloth first = new DurkwoodBaloth();
        DurkwoodBaloth second = new DurkwoodBaloth();
        harness.setHand(player2, List.of(first, second));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Durkwood Baloth");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst())
                .isNotSameAs(gd.playerGraveyards.get(player2.getId()).getFirst());
    }

    @Test
    @DisplayName("An empty hand still allows the ability to finish")
    void resolvesAgainstEmptyHand() {
        addReadyDementiaSliver(player1);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dementia Sliver");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("The ability can activate during its controller's end step")
    void canActivateDuringOwnEndStep() {
        addReadyDementiaSliver(player1);
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player2, List.of(card));
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Durkwood Baloth");

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("A summoning-sick Sliver cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new DementiaSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An activated granted ability still resolves after Dementia Sliver leaves")
    void grantedAbilitySurvivesGrantSourceLeaving() {
        harness.addToBattlefield(player1, new DementiaSliver());
        addReadySliver(player1);
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Durkwood Baloth");

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Card-name options do not change when unrevealed opposing cards change")
    void nameChoiceDoesNotExposeHiddenHandContents() {
        addReadyDementiaSliver(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new DurkwoodBaloth()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        List<String> firstOptions = gd.interaction
                .activeInteraction(PendingInteraction.ColorChoice.class).options();
        harness.handleListChoice(player1, "Dementia Sliver");
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(false);
        harness.setHand(player2, List.of(new SidewinderSliver()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyElementsOf(firstOptions);
    }

    private void addReadyDementiaSliver(Player player) {
        addReadySliver(player, new DementiaSliver());
    }

    private void addReadySliver(Player player) {
        addReadySliver(player, new SidewinderSliver());
    }

    private void addReadySliver(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
    }
}
