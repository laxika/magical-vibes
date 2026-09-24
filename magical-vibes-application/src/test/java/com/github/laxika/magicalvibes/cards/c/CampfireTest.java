package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Campfire.class, EdgarMarkov.class, GrizzlyBears.class})
class CampfireTest extends BaseCardTest {

    @Test
    void gainsTwoLife() {
        harness.addToBattlefield(player1, new Campfire());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void returnsAllCommandersFromCommandZoneAndGraveyardThenShufflesRemainingGraveyard() {
        Campfire campfire = new Campfire();
        harness.addToBattlefield(player1, campfire);

        Card commandZoneCommander = new EdgarMarkov();
        Card graveyardCommander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commandZoneCommander);
        gd.playerCommanders.get(player1.getId()).add(graveyardCommander);
        gd.playerCommandZones.get(player1.getId()).add(commandZoneCommander);
        harness.setGraveyard(player1, List.of(graveyardCommander, new GrizzlyBears()));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(commandZoneCommander, graveyardCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears || card == graveyardCommander);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == campfire);
    }
}
