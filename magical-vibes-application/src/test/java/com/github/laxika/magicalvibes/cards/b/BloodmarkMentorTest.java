package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodmarkMentorTest extends BaseCardTest {

    // ===== Grant: "Red creatures you control have first strike" =====

    @Test
    @DisplayName("Bloodmark Mentor grants itself first strike (it is red)")
    void grantsSelfFirstStrike() {
        Permanent mentor = addReady(player1, new BloodmarkMentor());

        assertThat(gqs.hasKeyword(gd, mentor, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to another red creature you control, and revokes it when it leaves")
    void grantsFirstStrikeToOtherRedCreature() {
        Permanent mentor = addReady(player1, new BloodmarkMentor());
        Permanent redCreature = addReady(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mentor);

        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to a non-red creature")
    void doesNotGrantToNonRedCreature() {
        addReady(player1, new BloodmarkMentor());
        Permanent greenCreature = addReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, greenCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to an opponent's red creature")
    void doesNotGrantToOpponentRedCreature() {
        addReady(player1, new BloodmarkMentor());
        Permanent opponentRed = addReady(player2, new HillGiant());

        assertThat(gqs.hasKeyword(gd, opponentRed, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
