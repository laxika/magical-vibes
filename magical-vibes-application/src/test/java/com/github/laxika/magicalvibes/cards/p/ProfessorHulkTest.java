package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfessorHulk.class, Forest.class})
class ProfessorHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to combat damage dealt to a player")
    void drawsEqualToCombatDamage() {
        Permanent hulk = addCreatureReady(player1, new ProfessorHulk());
        hulk.setAttacking(true);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 6);
    }
}
