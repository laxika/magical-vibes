package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokratesAthenianTeacher.class, HillGiant.class, Forest.class})
class SokratesAthenianTeacherTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof while untapped")
    void hasHexproofWhileUntapped() {
        Permanent sokrates = harness.addToBattlefieldAndReturn(player1, new SokratesAthenianTeacher());

        assertThat(gqs.hasKeyword(gd, sokrates, Keyword.HEXPROOF)).isTrue();

        sokrates.tap();

        assertThat(gqs.hasKeyword(gd, sokrates, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage and both players draw half, rounded down")
    void preventsCombatDamageAndDrawsHalf() {
        Permanent sokrates = harness.addToBattlefieldAndReturn(player1, new SokratesAthenianTeacher());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        sokrates.setSummoningSick(false);
        hillGiant.setSummoningSick(false);

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, hillGiant.getId());
        harness.passBothPriorities();

        hillGiant.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(player1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(player2HandBefore + 1);
        assertThat(sokrates.isTapped()).isTrue();
    }
}
