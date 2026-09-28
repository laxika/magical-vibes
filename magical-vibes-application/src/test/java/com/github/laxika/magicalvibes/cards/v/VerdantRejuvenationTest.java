package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        HillGiant.class
})
class VerdantRejuvenationTest extends BaseCardTest {

    @Test
    void seeksEligibleCardsUpToHighestControlledCreatureManaValue() {
        Card creature = new GrizzlyBears();
        Card enchantment = new HeartbeatOfSpring();
        Card land = new Forest();

        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(creature, enchantment, land));
        harness.setHand(player1, List.of(new VerdantRejuvenation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Heartbeat of Spring");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void doesNothingWhenYouControlNoCreatures() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new VerdantRejuvenation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
