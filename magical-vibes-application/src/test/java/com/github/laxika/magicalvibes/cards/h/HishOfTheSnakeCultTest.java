package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KomaCosmosSerpent;
import com.github.laxika.magicalvibes.cards.n.NagaOracle;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HishOfTheSnakeCult.class, NagaOracle.class, KomaCosmosSerpent.class, GrizzlyBears.class})
class HishOfTheSnakeCultTest extends BaseCardTest {

    @Test
    @DisplayName("Turns your Nagas and Serpents into Snakes and grants Snake abilities")
    void grantsSnakeAbilitiesToNagasAndSerpents() {
        addCreatureReady(player1, new HishOfTheSnakeCult());
        Permanent naga = addCreatureReady(player1, new NagaOracle());
        Permanent serpent = addCreatureReady(player1, new KomaCosmosSerpent());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        for (Permanent snake : List.of(findPermanent(player1, "Hish of the Snake Cult"), naga, serpent)) {
            assertThat(gqs.effectiveCreatureSubtypes(gd, snake)).contains(CardSubtype.SNAKE);
            assertThat(gqs.hasKeyword(gd, snake, Keyword.DEATHTOUCH)).isTrue();
            assertThat(gqs.hasKeyword(gd, snake, Keyword.POISONOUS)).isTrue();
        }
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear)).doesNotContain(CardSubtype.SNAKE);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Snakes deal two poison counters when they deal combat damage to a player")
    void poisonousTwoDealsTwoPoisonCounters() {
        Permanent hish = addCreatureReady(player1, new HishOfTheSnakeCult());
        hish.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Daunt prevents creatures with power two or less from blocking Snakes")
    void dauntPreventsSmallBlockers() {
        Permanent hish = addCreatureReady(player1, new HishOfTheSnakeCult());
        hish.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(hish)))))
                .isInstanceOf(IllegalStateException.class);
    }
}
