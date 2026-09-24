package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.n.NoxiousRevival;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurktideRegent.class, Shock.class, LavaAxe.class, GrizzlyBears.class,
        Forest.class, NoxiousRevival.class})
class MurktideRegentTest extends BaseCardTest {

    @Test
    void entersWithCountersForExiledInstantsAndSorceries() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new Shock(), new LavaAxe(), new GrizzlyBears(), new Forest(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        Permanent murktide = findPermanent(player1, "Murktide Regent");
        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsACounterForEachInstantOrSorceryLeavingGraveyard() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new Shock(), new LavaAxe(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new MurktideRegent(), new NoxiousRevival(),
                new NoxiousRevival()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent murktide = findPermanent(player1, "Murktide Regent");
        UUID shockId = gd.playerGraveyards.get(player1.getId()).get(0).getId();
        harness.castInstant(player1, 0, shockId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID lavaAxeId = gd.playerGraveyards.get(player1.getId()).get(0).getId();
        harness.castInstant(player1, 0, lavaAxeId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
