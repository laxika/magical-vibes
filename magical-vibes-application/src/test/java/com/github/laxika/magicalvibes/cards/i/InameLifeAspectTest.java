package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HundredTalonKami;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InameLifeAspectTest extends BaseCardTest {

    /** Kills Iname with a Wrath of God cast by player 1 and resolves it. */
    private Card killIname() {
        harness.addToBattlefield(player1, new InameLifeAspect());
        Permanent iname = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card inameCard = iname.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
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
        Card bears = new GrizzlyBears();
        Card lanternKami = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, lanternKami)));

        Card inameCard = killIname();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        // Iname is itself a Spirit card in the graveyard, so it is a legal target too.
        assertThat(choice.validCardIds()).contains(lanternKami.getId(), inameCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(bears.getId());
    }
}
