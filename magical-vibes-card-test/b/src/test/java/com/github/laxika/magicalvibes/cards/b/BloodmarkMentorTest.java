package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IntimidatorInitiate;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.cards.t.TattermungeManiac;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodmarkMentor.class, IntimidatorInitiate.class, SafeholdSentry.class, TattermungeManiac.class})
class BloodmarkMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodmark Mentor grants itself first strike (it is red)")
    void grantsSelfFirstStrike() {
        Permanent mentor = addCreatureReady(player1, new BloodmarkMentor());

        assertThat(gqs.hasKeyword(gd, mentor, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to another red creature you control, and revokes it when it leaves")
    void grantsFirstStrikeToOtherRedCreature() {
        Permanent mentor = addCreatureReady(player1, new BloodmarkMentor());
        Permanent redCreature = addCreatureReady(player1, new IntimidatorInitiate());

        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mentor);

        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to a non-red creature")
    void doesNotGrantToNonRedCreature() {
        addCreatureReady(player1, new BloodmarkMentor());
        Permanent nonRedCreature = addCreatureReady(player1, new SafeholdSentry());

        assertThat(gqs.hasKeyword(gd, nonRedCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to an opponent's red creature")
    void doesNotGrantToOpponentRedCreature() {
        addCreatureReady(player1, new BloodmarkMentor());
        Permanent opponentRed = addCreatureReady(player2, new IntimidatorInitiate());

        assertThat(gqs.hasKeyword(gd, opponentRed, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Grants first strike to a red and green creature")
    void grantsFirstStrikeToMulticoloredRedCreature() {
        addCreatureReady(player1, new BloodmarkMentor());
        Permanent creature = addCreatureReady(player1, new TattermungeManiac());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A creature already on the battlefield gains first strike when the Mentor arrives")
    void grantsFirstStrikeToExistingCreature() {
        Permanent creature = addCreatureReady(player1, new IntimidatorInitiate());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        addCreatureReady(player1, new BloodmarkMentor());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike remains until the last Mentor leaves the battlefield")
    void overlappingMentorsGrantFirstStrikeIndependently() {
        Permanent firstMentor = addCreatureReady(player1, new BloodmarkMentor());
        Permanent secondMentor = addCreatureReady(player1, new BloodmarkMentor());
        Permanent creature = addCreatureReady(player1, new IntimidatorInitiate());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstMentor);

        assertThat(gqs.hasKeyword(gd, secondMentor, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondMentor);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }
}
