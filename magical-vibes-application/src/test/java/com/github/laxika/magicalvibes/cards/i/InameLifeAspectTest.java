package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.h.HundredTalonKami;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InameLifeAspect.class, RendSpirit.class, RendFlesh.class,
        LanternKami.class, HundredTalonKami.class})
class InameLifeAspectTest extends BaseCardTest {

    /** Kills Iname with a Rend Spirit cast by player 1 and resolves it. */
    private Card killIname() {
        Permanent inamePermanent = harness.addToBattlefieldAndReturn(player1, new InameLifeAspect());
        Card inameCard = inamePermanent.getCard();

        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, inamePermanent.getId());
        return inameCard;
    }

    @Test
    @DisplayName("Exiling Iname returns any number of targeted Spirit cards from your graveyard to your hand")
    void exileReturnsTargetedSpirits() {
        Card lanternKami = new LanternKami();
        Card hundredTalonKami = new HundredTalonKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(lanternKami, hundredTalonKami)));

        Card inameCard = killIname();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(lanternKami.getId(), hundredTalonKami.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(lanternKami.getId(), hundredTalonKami.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(inameCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(inameCard.getId()));
    }

    @Test
    @DisplayName("Choosing no Spirit cards still allows Iname to exile itself")
    void choosingNoTargetsStillExilesIname() {
        Card inameCard = killIname();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(inameCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(inameCard.getId()));
    }

    @Test
    @DisplayName("Targeting Iname itself does not return it after the exile")
    void targetingInameDoesNotReturnIt() {
        Card inameCard = killIname();

        harness.handleMultipleCardsChosen(player1, List.of(inameCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(inameCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(inameCard.getId()));
    }

    @Test
    @DisplayName("Moving Iname out of the graveyard before resolution prevents the return")
    void leavingGraveyardBeforeResolutionPreventsReturn() {
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        Card inameCard = killIname();
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.setGraveyard(player1, List.of(spirit));
        harness.setHand(player1, List.of(inameCard));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(spirit.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Declining the exile leaves Iname and the targeted Spirit in the graveyard")
    void decliningExileReturnsNothing() {
        Card lanternKami = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(lanternKami)));

        Card inameCard = killIname();

        harness.handleMultipleCardsChosen(player1, List.of(lanternKami.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(inameCard.getId(), lanternKami.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(lanternKami.getId()));
    }

    @Test
    @DisplayName("Only Spirit cards in your graveyard are legal targets")
    void nonSpiritCardIsNotALegalTarget() {
        Card nonSpirit = new RendFlesh();
        Card lanternKami = new LanternKami();
        Card opponentSpirit = new HundredTalonKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(nonSpirit, lanternKami)));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        Card inameCard = killIname();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        // Iname is itself a Spirit card in the graveyard, so it is a legal target too.
        assertThat(choice.validCardIds()).contains(lanternKami.getId(), inameCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonSpirit.getId(), opponentSpirit.getId());
    }
}
