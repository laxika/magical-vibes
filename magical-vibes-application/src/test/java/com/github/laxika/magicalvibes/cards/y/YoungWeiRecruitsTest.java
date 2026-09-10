package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoungWeiRecruits.class, ForestBear.class})
class YoungWeiRecruitsTest extends BaseCardTest {

    @Test
    @DisplayName("Young Wei Recruits cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new YoungWeiRecruits());

        var attacker = addCreatureReady(player1, new ForestBear());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
