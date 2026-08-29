package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParadoxicalOutcomeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen permanents and draws for each one returned")
    void returnsChosenPermanentsAndDrawsForEach() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new ParadoxicalOutcome()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Island");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Fountain of Youth", "Forest", "Island");
    }

    @Test
    @DisplayName("Can resolve with no targets and draws no cards")
    void resolvesWithNoTargets() {
        harness.setHand(player1, List.of(new ParadoxicalOutcome()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot target a land or an opponent's permanent")
    void rejectsIllegalTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new ParadoxicalOutcome()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ParadoxicalOutcome()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a token")
    void rejectsTokenTarget() {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setHand(player1, List.of(new ParadoxicalOutcome()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(token.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
