package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoughshodMentor.class, SafeholdElite.class, AshenmoorCohort.class})
class RoughshodMentorTest extends BaseCardTest {

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
        Permanent greenCreature = addCreatureReady(player1, new SafeholdElite());

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
        Permanent opponentGreen = addCreatureReady(player2, new SafeholdElite());

        assertThat(gqs.hasKeyword(gd, opponentGreen, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A green and white creature already on the battlefield gains trample when Mentor enters")
    void grantsTrampleToExistingMulticoloredCreature() {
        Permanent creature = addCreatureReady(player1, new SafeholdElite());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        addCreatureReady(player1, new RoughshodMentor());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample remains until the last Roughshod Mentor leaves")
    void remainingMentorContinuesGrantingTrample() {
        Permanent firstMentor = addCreatureReady(player1, new RoughshodMentor());
        Permanent secondMentor = addCreatureReady(player1, new RoughshodMentor());
        Permanent creature = addCreatureReady(player1, new SafeholdElite());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstMentor);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondMentor, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondMentor);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }
}
