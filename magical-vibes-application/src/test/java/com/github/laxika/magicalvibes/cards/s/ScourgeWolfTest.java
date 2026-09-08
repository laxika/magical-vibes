package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScourgeWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike with fewer than four card types in its controller's graveyard")
    void noDoubleStrikeBelowDelirium() {
        harness.addToBattlefield(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(new Plains(), new LeoninScimitar(), new Pacifism()));

        Permanent wolf = findPermanent(player1, "Scourge Wolf");

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with four card types in its controller's graveyard")
    void hasDoubleStrikeWithDelirium() {
        harness.addToBattlefield(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));

        Permanent wolf = findPermanent(player1, "Scourge Wolf");

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not count card types in an opponent's graveyard")
    void opponentGraveyardDoesNotCount() {
        harness.addToBattlefield(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(new Plains(), new Shock(), new LeoninScimitar()));
        harness.setGraveyard(player2, List.of(new Pacifism()));

        Permanent wolf = findPermanent(player1, "Scourge Wolf");

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
