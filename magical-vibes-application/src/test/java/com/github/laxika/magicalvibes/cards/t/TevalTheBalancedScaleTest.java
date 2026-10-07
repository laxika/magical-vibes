package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BojukaBog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TevalTheBalancedScale.class, Forest.class, GrizzlyBears.class, BojukaBog.class})
class TevalTheBalancedScaleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills three, then may return a land tapped and creates a Zombie Druid")
    void attackingMillsThenReturnsLandAndCreatesToken() {
        Forest land = new Forest();
        List<Card> milledCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, milledCards);
        harness.setGraveyard(player1, List.of(land));
        addReadyTeval();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);

        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Zombie Druid");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactly(CardSubtype.ZOMBIE, CardSubtype.DRUID);
    }

    @Test
    @DisplayName("Declining the land return still mills but does not create a token")
    void decliningLandReturnStillMills() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(land));
        addReadyTeval();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(findPermanents(player1, "Zombie Druid")).isEmpty();
    }

    @Test
    @DisplayName("The land returned may be one of the cards just milled")
    void returnsNewlyMilledLandFromShortLibrary() {
        BojukaBog land = new BojukaBog();
        harness.setLibrary(player1, List.of(land, new TevalTheBalancedScale()));
        harness.setGraveyard(player1, List.of());
        addReadyTeval();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land).hasSize(1);
        assertThat(findPermanent(player1, "Bojuka Bog").isTapped()).isTrue();
        assertThat(countPermanents(player1, "Zombie Druid")).isEqualTo(1);
    }

    @Test
    @DisplayName("Milling with no available land finishes without returning a creature")
    void noLandAvailableDoesNotReturnNonland() {
        TevalTheBalancedScale milledCard = new TevalTheBalancedScale();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setGraveyard(player1, List.of());
        addReadyTeval();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledCard);
        assertThat(countPermanents(player1, "Teval, the Balanced Scale")).isEqualTo(1);
        assertThat(findPermanents(player1, "Zombie Druid")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiling several cards together creates exactly one token")
    void simultaneousGraveyardExileCreatesOneToken() {
        List<Card> cards = List.of(new TevalTheBalancedScale(), new BojukaBog());
        harness.setGraveyard(player1, cards);
        harness.addToBattlefield(player1, new TevalTheBalancedScale());
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(countPermanents(player1, "Zombie Druid")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cards leaving an opponent's graveyard do not create a token")
    void opponentsGraveyardDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new TevalTheBalancedScale()));
        harness.addToBattlefield(player1, new TevalTheBalancedScale());
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Zombie Druid")).isEmpty();
    }

    @Test
    @DisplayName("Exiling an empty graveyard does not create a token")
    void emptyGraveyardDoesNotTrigger() {
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player1, new TevalTheBalancedScale());
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie Druid")).isEmpty();
    }

    private Permanent addReadyTeval() {
        return addCreatureReady(player1, new TevalTheBalancedScale());
    }
}
