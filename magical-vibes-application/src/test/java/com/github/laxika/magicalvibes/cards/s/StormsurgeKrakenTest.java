package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormsurgeKraken.class, EdgarMarkov.class, GrizzlyBears.class})
class StormsurgeKrakenTest extends BaseCardTest {

    @Test
    void lieutenantBoostsStormsurgeKraken() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(7);
    }

    @Test
    void noCommanderMeansNoLieutenantBonusOrTrigger() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void lieutenantMayDrawTwoCardsWhenBlocked() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
