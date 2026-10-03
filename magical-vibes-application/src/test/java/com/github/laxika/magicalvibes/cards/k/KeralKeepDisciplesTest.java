package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GarrukUnleashed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeralKeepDisciples.class, ChandraNalaar.class, GarrukUnleashed.class})
class KeralKeepDisciplesTest extends BaseCardTest {

    @Test
    void chandraLoyaltyAbilityDealsDamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KeralKeepDisciples());
        Permanent chandra = addReadyPlaneswalker(player1, new ChandraNalaar(), 4);

        harness.activateAbility(player1, battlefieldIndex(player1, chandra), null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void nonChandraLoyaltyAbilityDoesNotTrigger() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KeralKeepDisciples());
        Permanent garruk = addReadyPlaneswalker(player1, new GarrukUnleashed(), 4);

        harness.activateAbility(player1, battlefieldIndex(player1, garruk), 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addReadyPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return planeswalker;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
