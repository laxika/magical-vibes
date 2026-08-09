package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AjanisInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on the target creature and offers a white card")
    void putsCountersAndOffersWhiteCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        setupTopCards(List.of(new Shock(), pacifism, new Island(), new Swamp(), new GrizzlyBears()));

        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactly(pacifism.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Puts the chosen white card into hand and the rest on the library bottom")
    void choosesWhiteCardAndRandomizesTheRest() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        List<Card> topCards = List.of(pacifism, new Shock(), new Island(), new Swamp(), new GrizzlyBears());
        setupTopCards(topCards);

        cast(target.getId());
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(pacifism.getId())));

        assertThat(gd.playerHands.get(player1.getId())).contains(pacifism);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards.subList(1, 5));
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("May decline the white card and put all five cards on the library bottom")
    void mayDeclineWhiteCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> topCards = List.of(new Pacifism(), new Shock(), new Island(), new Swamp(), new GrizzlyBears());
        setupTopCards(topCards);

        cast(target.getId());
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new AjanisInfluence()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(cards);
    }
}
