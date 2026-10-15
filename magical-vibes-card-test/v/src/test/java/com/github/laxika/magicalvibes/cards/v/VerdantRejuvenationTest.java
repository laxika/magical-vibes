package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProsperousInnkeeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        VerdantRejuvenation.class,
        Forest.class,
        GrizzlyBears.class,
        HeartbeatOfSpring.class,
        HillGiant.class,
        ProsperousInnkeeper.class
})
class VerdantRejuvenationTest extends BaseCardTest {

    @Test
    void seeksEligibleCardsUpToHighestControlledCreatureManaValue() {
        Card creature = new GrizzlyBears();
        Card enchantment = new HeartbeatOfSpring();
        Card land = new Forest();

        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(creature, enchantment, land));
        harness.castFromHand(player1, new VerdantRejuvenation(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Heartbeat of Spring");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void doesNothingWhenYouControlNoCreatures() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.castFromHand(player1, new VerdantRejuvenation(), "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void soughtCreaturesSeeEachOtherEnterSimultaneously() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new ProsperousInnkeeper(), new ProsperousInnkeeper()));

        harness.castFromHand(player1, new VerdantRejuvenation(), "{6}{G}{G}");
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void seeksExactlyTheGreatestManaValueAndPreservesRemainingLibraryOrder() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card thirdLand = new Forest();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card thirdCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(firstLand, firstCreature, secondLand,
                secondCreature, thirdLand, thirdCreature));

        harness.castFromHand(player1, new VerdantRejuvenation(), "{6}{G}{G}");
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining).hasSize(4);
        assertThat(remaining.stream().filter(card -> card instanceof Forest).toList())
                .containsExactly(firstLand, secondLand, thirdLand);
        assertThat(remaining.stream().filter(card -> card instanceof GrizzlyBears).toList())
                .hasSize(1);
    }
}
