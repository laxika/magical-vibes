package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RattleblazeScarecrowTest extends BaseCardTest {

    // ===== Persist: "as long as you control a black creature" =====

    @Test
    @DisplayName("Has persist while controlling a black creature")
    void hasPersistWithBlackCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player1, new AshenmoorCohort()); // black

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Loses persist when no black creature is controlled")
    void noPersistWithoutBlackCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("An opponent's black creature does not grant persist")
    void opponentBlackCreatureDoesNotGrantPersist() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player2, new AshenmoorCohort()); // opponent's black

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    // ===== Haste: "as long as you control a red creature" =====

    @Test
    @DisplayName("Has haste while controlling a red creature")
    void hasHasteWithRedCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player1, new HillGiant()); // red

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not have haste when no red creature is controlled")
    void noHasteWithoutRedCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player1, new AshenmoorCohort()); // black, not red

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }
}
