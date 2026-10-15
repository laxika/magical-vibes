package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImprobableAlliance.class, GrizzlyBears.class, Forest.class})
class ImprobableAllianceTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a 1/1 blue Faerie token with flying")
    void secondDrawCreatesFaerieToken() {
        harness.addToBattlefieldAndReturn(player1, new ImprobableAlliance());
        addCardsToDeck(2);

        draw();
        draw();
        resolveTopOfStack();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The second-card trigger does not fire again during the same turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new ImprobableAlliance());
        addCardsToDeck(3);

        draw();
        draw();
        resolveTopOfStack();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability draws a card, then prompts for a discard")
    void activatedAbilityLoots() {
        harness.addToBattlefield(player1, new ImprobableAlliance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().extracting(Card::getName)
                .isEqualTo("Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void firstDrawDoesNotCreateToken() {
        harness.addToBattlefield(player1, new ImprobableAlliance());
        addCardsToDeck(1);

        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Faerie")).isZero();
    }

    @Test
    void opponentsDrawsDoNotTriggerAlliance() {
        harness.addToBattlefield(player1, new ImprobableAlliance());
        setDeck(player2, List.of(new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player2.getId(), 2));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Faerie")).isZero();
        assertThat(countPermanents(player2, "Faerie")).isZero();
    }

    @Test
    void secondDrawOnOpponentsTurnCreatesToken() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new ImprobableAlliance());
        addCardsToDeck(2);

        draw();
        draw();
        resolveTopOfStack();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Faerie")).isZero();
    }

    @Test
    void countsDrawBeforeAllianceEnteredBattlefield() {
        addCardsToDeck(2);
        draw();
        harness.addToBattlefield(player1, new ImprobableAlliance());

        draw();
        resolveTopOfStack();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        addCardsToDeck(3);
        draw();
        draw();
        harness.addToBattlefield(player1, new ImprobableAlliance());

        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Faerie")).isZero();
    }

    @Test
    void eachAllianceTriggersForSameSecondDraw() {
        harness.addToBattlefield(player1, new ImprobableAlliance());
        harness.addToBattlefield(player1, new ImprobableAlliance());
        addCardsToDeck(2);

        draw();
        draw();
        resolveTopOfStack();
        resolveTopOfStack();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(2);
    }

    @Test
    void activatedSecondDrawFinishesDiscardBeforeTokenResolves() {
        harness.addToBattlefield(player1, new ImprobableAlliance());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new Forest()));
        draw();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(countPermanents(player1, "Faerie")).isZero();

        harness.handleCardChosen(player1, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addCardsToDeck(int count) {
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
