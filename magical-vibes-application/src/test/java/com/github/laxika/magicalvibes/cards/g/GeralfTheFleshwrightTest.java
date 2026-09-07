package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeralfTheFleshwright.class, Gravecrawler.class, LightningBolt.class})
class GeralfTheFleshwrightTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie Rogue for every spell after the first during your turn")
    void createsTokenForEachSpellAfterFirstDuringYourTurn() {
        harness.addToBattlefield(player1, new GeralfTheFleshwright());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(tokenCount()).isZero();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(tokenCount()).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(tokenCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not create tokens for spells cast during an opponent's turn")
    void doesNotCreateTokensDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GeralfTheFleshwright());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(tokenCount()).isZero();
    }

    @Test
    @DisplayName("Counts a spell cast before Geralf entered the battlefield")
    void countsSpellCastBeforeEnteringBattlefield() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.addToBattlefield(player1, new GeralfTheFleshwright());

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts counters on a Zombie for each other Zombie that entered this turn")
    void putsCountersForOtherZombiesEnteredThisTurn() {
        harness.addToBattlefield(player1, new GeralfTheFleshwright());
        harness.setHand(player1, List.of(new Gravecrawler(), new Gravecrawler()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> crawlers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Gravecrawler)
                .toList();
        assertThat(crawlers).hasSize(2);
        assertThat(crawlers.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(crawlers.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private long tokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
