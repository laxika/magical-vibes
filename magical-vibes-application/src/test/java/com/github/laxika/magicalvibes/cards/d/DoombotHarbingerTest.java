package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoombotHarbinger.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
class DoombotHarbingerTest extends BaseCardTest {

    @Test
    void mayMillFourCardsWhenItEnters() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new DoombotHarbinger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId(), third.getId(), fourth.getId());
    }

    @Test
    void mayExileItselfAndReturnTargetCreatureCardToHand() {
        Card doombot = addDoombotToBattlefield();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        destroyDoombot();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(target.getId(), doombot.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(doombot.getId());
    }

    @Test
    void decliningSelfExileLeavesSourceAndTargetInGraveyard() {
        Card doombot = addDoombotToBattlefield();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        destroyDoombot();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(doombot.getId(), target.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId());
    }

    private Card addDoombotToBattlefield() {
        Card doombot = new DoombotHarbinger();
        harness.addToBattlefield(player1, doombot);
        return doombot;
    }

    private void destroyDoombot() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
