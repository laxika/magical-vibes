package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrippingDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbalmedBrawler.class, DrippingDead.class, GrizzlyBears.class})
class EmbalmedBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each Zombie card in its controller's hand")
    void entersWithCountersForZombiesInHand() {
        EmbalmedBrawler brawler = new EmbalmedBrawler();
        harness.setHand(player1, List.of(
                brawler, new DrippingDead(), new DrippingDead(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new DrippingDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(brawler).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Its attack trigger makes its controller lose life equal to its counters")
    void losesLifeWhenAttacking() {
        Permanent brawler = addCreatureReady(player1, new EmbalmedBrawler());
        brawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Its block trigger makes its controller lose life equal to its counters")
    void losesLifeWhenBlocking() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent brawler = addCreatureReady(player2, new EmbalmedBrawler());
        brawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent findPermanentForCard(EmbalmedBrawler card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
