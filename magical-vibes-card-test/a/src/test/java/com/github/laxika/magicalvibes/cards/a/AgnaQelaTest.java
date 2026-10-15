package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgnaQela.class, Forest.class, Island.class})
class AgnaQelaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no basic land")
    void entersTappedWithoutBasicLand() {
        playAgnaQela(player1);

        assertThat(findPermanent(player1, "Agna Qel'a").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());

        playAgnaQela(player1);

        assertThat(findPermanent(player1, "Agna Qel'a").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonbasic land does not satisfy the basic-land check")
    void nonbasicLandDoesNotSatisfyCheck() {
        harness.addToBattlefield(player1, new AgnaQela());

        playAgnaQela(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent agnaQela = addReadyAgnaQela(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(agnaQela.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability draws a card then discards a card")
    void activatedAbilityDrawsThenDiscards() {
        addReadyAgnaQela(player1);
        Card discarded = new Forest();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    private void playAgnaQela(Player player) {
        harness.setHand(player, List.of(new AgnaQela()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addReadyAgnaQela(Player player) {
        Permanent agnaQela = harness.addToBattlefieldAndReturn(player, new AgnaQela());
        agnaQela.setSummoningSick(false);
        return agnaQela;
    }

    @Test
    @DisplayName("An opponent's basic land does not allow this land to enter untapped")
    void opponentsBasicLandDoesNotSatisfyCheck() {
        harness.addToBattlefield(player2, new Island());

        playAgnaQela(player1);

        assertThat(findPermanent(player1, "Agna Qel'a").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped basic land still allows this land to enter untapped")
    void tappedBasicLandSatisfiesCheck() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();

        playAgnaQela(player1);

        assertThat(findPermanent(player1, "Agna Qel'a").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may discard the newly drawn card instead of a card already in hand")
    void mayDiscardNewlyDrawnCard() {
        addReadyAgnaQela(player1);
        Card retained = new Forest();
        Card drawn = new Island();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(drawn, new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("With an empty hand, the drawn card is discarded after paying the activation costs")
    void emptyHandDiscardsDrawnCard() {
        Permanent land = addReadyAgnaQela(player1);
        Card drawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
