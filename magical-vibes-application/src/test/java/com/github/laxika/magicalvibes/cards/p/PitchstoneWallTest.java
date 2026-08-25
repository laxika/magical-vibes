package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitchstoneWall.class, Sift.class, GrizzlyBears.class})
class PitchstoneWallTest extends BaseCardTest {

    @Test
    void acceptingMaySacrificesWallAndReturnsDiscardedCard() {
        Card discardedCard = prepareDiscard();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Pitchstone Wall");
        harness.assertNotInGraveyard(player1, discardedCard.getName());
        assertThat(gd.playerHands.get(player1.getId())).contains(discardedCard);
    }

    @Test
    void decliningMayKeepsWallAndLeavesDiscardedCardInGraveyard() {
        Card discardedCard = prepareDiscard();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Pitchstone Wall");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discardedCard);
    }

    private Card prepareDiscard() {
        harness.addToBattlefield(player1, new PitchstoneWall());
        Card discardedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Sift(), discardedCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        return discardedCard;
    }
}
