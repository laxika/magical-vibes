package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonbornChampion.class, Forest.class})
class DragonbornChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a controlled source deals exactly 5 damage")
    void drawsAtThreshold() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        Permanent champion = addCreatureReady(player1, new DragonbornChampion());
        champion.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a controlled source deals less than 5 damage")
    void doesNotDrawBelowThreshold() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        DragonbornChampion card = new DragonbornChampion();
        card.setPower(4);
        Permanent champion = addCreatureReady(player1, card);
        champion.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
