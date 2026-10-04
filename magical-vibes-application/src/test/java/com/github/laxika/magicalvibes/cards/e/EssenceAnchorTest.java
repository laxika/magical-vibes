package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HeritageReclamation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceAnchor.class, HeritageReclamation.class})
class EssenceAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of its controller's upkeep")
    void upkeepSurveilsOne() {
        harness.addToBattlefield(player1, new EssenceAnchor());
        Card topCard = new HeritageReclamation();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Creates a 2/2 black Zombie Druid token after a card left the graveyard this turn")
    void createsZombieDruidTokenWhenCardLeftGraveyard() {
        forceMainPhase();
        harness.addToBattlefield(player1, new EssenceAnchor());
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie Druid");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.DRUID);
    }

    @Test
    @DisplayName("Cannot activate without a card leaving the graveyard this turn")
    void cannotActivateWithoutCardLeavingGraveyard() {
        forceMainPhase();
        harness.addToBattlefield(player1, new EssenceAnchor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("card left your graveyard this turn");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new EssenceAnchor());
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void surveilCanKeepTopCardWithoutEnablingActivation() {
        harness.addToBattlefield(player1, new EssenceAnchor());
        Card topCard = new HeritageReclamation();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("card left your graveyard this turn");
    }

    @Test
    void puttingSurveilledCardIntoGraveyardDoesNotEnableActivation() {
        harness.addToBattlefield(player1, new EssenceAnchor());
        Card topCard = new HeritageReclamation();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("card left your graveyard this turn");
    }

    @Test
    void doesNotSurveilDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EssenceAnchor());
        Card topCard = new HeritageReclamation();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void exilingNoncreatureFromOwnGraveyardEnablesActivationDuringEndStep() {
        forceMainPhase();
        Permanent anchor = harness.addToBattlefieldAndReturn(player1, new EssenceAnchor());
        Card graveyardCard = new EssenceAnchor();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 2, graveyardCard.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);

        harness.forceStep(TurnStep.END_STEP);
        harness.activateAbility(player1, 0, null, null);
        assertThat(anchor.isTapped()).isTrue();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(countPermanents(player1, "Zombie Druid")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie Druid")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilingCardFromOpponentsGraveyardDoesNotEnableActivation() {
        forceMainPhase();
        harness.addToBattlefield(player1, new EssenceAnchor());
        Card graveyardCard = new EssenceAnchor();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, 2, graveyardCard.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("card left your graveyard this turn");
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
