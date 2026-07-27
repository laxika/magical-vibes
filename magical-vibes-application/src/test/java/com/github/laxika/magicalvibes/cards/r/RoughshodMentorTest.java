package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoughshodMentorTest extends BaseCardTest {

    // ===== Grant: "Green creatures you control have trample" =====

    @Test
    @DisplayName("Roughshod Mentor grants itself trample (it is green)")
    void grantsSelfTrample() {
        Permanent mentor = addCreatureReady(player1, new RoughshodMentor());

        assertThat(gqs.hasKeyword(gd, mentor, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to another green creature you control, and revokes it when it leaves")
    void grantsTrampleToOtherGreenCreature() {
        Permanent mentor = addCreatureReady(player1, new RoughshodMentor());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, greenCreature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mentor);

        assertThat(gqs.hasKeyword(gd, greenCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to a non-green creature")
    void doesNotGrantToNonGreenCreature() {
        addCreatureReady(player1, new RoughshodMentor());
        Permanent blackCreature = addCreatureReady(player1, new AshenmoorCohort());

        assertThat(gqs.hasKeyword(gd, blackCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to an opponent's green creature")
    void doesNotGrantToOpponentGreenCreature() {
        addCreatureReady(player1, new RoughshodMentor());
        Permanent opponentGreen = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentGreen, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Helpers =====
}
