package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepwoodTantiv.class, FreshVolunteers.class})
class DeepwoodTantivTest extends BaseCardTest {

    @Test
    @DisplayName("When Deepwood Tantiv becomes blocked, you gain 2 life once")
    void becomesBlockedGainsLifeOnce() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new DeepwoodTantiv());
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("When Deepwood Tantiv is unblocked, you gain no life")
    void unblockedDoesNotGainLife() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new DeepwoodTantiv());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }
}
