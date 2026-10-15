package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelvalaExplorerReturned.class, Forest.class, GrizzlyBears.class, Abundance.class})
class SelvalaExplorerReturnedTest extends BaseCardTest {

    private static final int STARTING_LIFE = GameData.STARTING_LIFE_TOTAL;

    @Test
    @DisplayName("Parley adds green mana and life for each revealed nonland, then draws")
    void parleyRewardsEachNonlandAndDraws() {
        Card topNonland = new GrizzlyBears();
        Card topLand = new Forest();
        Card nextPlayer1Card = new Forest();
        Card nextPlayer2Card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topNonland, nextPlayer1Card));
        harness.setLibrary(player2, List.of(topLand, nextPlayer2Card));
        Permanent selvala = addReadySelvala();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(selvala.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(topNonland);
        assertThat(gd.playerHands.get(player2.getId())).contains(topLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextPlayer1Card);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextPlayer2Card);
    }

    @Test
    @DisplayName("Parley draws revealed lands without adding mana or life")
    void parleyDrawsLandsWithoutReward() {
        Card player1Land = new Forest();
        Card player2Land = new Forest();
        harness.setLibrary(player1, List.of(player1Land));
        harness.setLibrary(player2, List.of(player2Land));
        addReadySelvala();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Land);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Land);
    }

    @Test
    @DisplayName("Parley waits on the stack before revealing cards or awarding mana and life")
    void parleyUsesTheStack() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, new Forest()));
        harness.setLibrary(player2, List.of(secondCard, new Forest()));
        Permanent selvala = addReadySelvala();

        harness.activateAbility(player1, 0, null, null);

        assertThat(selvala.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(firstCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(secondCard);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE + 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("An opponent-controlled Selvala rewards its controller for both nonlands")
    void opponentControllerReceivesRewards() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, new Forest()));
        harness.setLibrary(player2, List.of(secondCard, new Forest()));
        addCreatureReady(player2, new SelvalaExplorerReturned());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(STARTING_LIFE + 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("The active player completes their draw replacement before the other player draws")
    void activePlayerDrawsFirstWithAbundance() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(firstCard, new Forest()));
        harness.setLibrary(player2, List.of(secondCard, new GrizzlyBears()));
        addReadySelvala();
        harness.addToBattlefield(player2, new Abundance());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(firstCard);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE + 1);
    }

    @Test
    @DisplayName("Abundance changes the draw without changing the reward for the revealed card")
    void rewardsUseRevealedCardsBeforeDrawReplacement() {
        Card revealedLand = new Forest();
        Card replacementCard = new GrizzlyBears();
        Card opponentLand = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(revealedLand, replacementCard, new Forest()));
        harness.setLibrary(player2, List.of(opponentLand, new GrizzlyBears()));
        addReadySelvala();
        harness.addToBattlefield(player1, new Abundance());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(replacementCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentLand);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(revealedLand);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    @Test
    @DisplayName("Selvala cannot pay its tap cost while summoning sick")
    void summoningSicknessPreventsActivation() {
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaExplorerReturned());
        selvala.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(selvala.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Selvala cannot activate Parley again")
    void tappedSelvalaCannotActivateAgain() {
        Permanent selvala = addReadySelvala();
        selvala.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySelvala() {
        return addCreatureReady(player1, new SelvalaExplorerReturned());
    }
}
