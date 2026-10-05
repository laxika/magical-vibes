package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaylasCommand.class, Plains.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class KaylasCommandTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Construct and applies the counter and double strike to the chosen creature")
    void createsTokenAndEmpowersChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Construct");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(giant.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("searches for a basic Plains and then gains life and scries")
    void searchesAndGainsLifeAndScries() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("the Plains search does not offer a non-Plains card")
    void searchFiltersToBasicPlains() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2}, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Plains");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("the newly created Construct can receive the counter and double strike")
    void empowersNewlyCreatedConstruct() {
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 0}, List.of());
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(construct.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("the counter choice cannot be declined when creatures are available")
    void counterChoiceIsMandatory() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("the other mode still resolves when no creature is controlled")
    void gainsLifeWithoutOwnCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("a restricted Plains search may fail to find and still resolves the other mode")
    void mayDeclineToFindPlains() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new KaylasCommand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2}, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Construct");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
