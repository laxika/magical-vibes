package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BridledBighorn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShepherdOfTheClouds.class, BridledBighorn.class, GrizzlyBears.class, HolyDay.class, SerraAngel.class})
class ShepherdOfTheCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent card with mana value three or less to hand without a Mount")
    void returnsPermanentToHandWithoutMount() {
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
    }

    @Test
    @DisplayName("Returns the target permanent card to the battlefield when controlling a Mount")
    void returnsPermanentToBattlefieldWithMount() {
        harness.addToBattlefield(player1, new BridledBighorn());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    @DisplayName("Cannot target a permanent card with mana value greater than three")
    void cannotTargetHighManaValuePermanent() {
        Card returned = new SerraAngel();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new ShepherdOfTheClouds()));
        addShepherdMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card")
    void cannotTargetNonpermanentCard() {
        Card returned = new HolyDay();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new ShepherdOfTheClouds()));
        addShepherdMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
    }

    private void castShepherd(Card returned) {
        harness.setHand(player1, List.of(new ShepherdOfTheClouds()));
        addShepherdMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();
    }

    private void addShepherdMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
