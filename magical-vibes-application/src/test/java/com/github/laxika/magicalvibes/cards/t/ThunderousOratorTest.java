package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.FencingAce;
import com.github.laxika.magicalvibes.cards.f.FathomFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GarruksCompanion;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderousOratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gains each matching keyword when the attack trigger resolves")
    void gainsMatchingKeywordsAtResolution() {
        Permanent orator = addReadyCreature(player1, new ThunderousOrator());

        declareAttackers(player1, List.of(0));
        addReadyCreature(player1, new VampireNighthawk());
        addReadyCreature(player1, new WhiteKnight());
        addReadyCreature(player1, new FencingAce());
        addReadyCreature(player1, new DarksteelMyr());
        addReadyCreature(player1, new FathomFleetCaptain());
        addReadyCreature(player1, new GarruksCompanion());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain listed keywords without matching creatures")
    void doesNotGainKeywordsWithoutMatchers() {
        Permanent orator = addReadyCreature(player1, new ThunderousOrator());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent orator = addReadyCreature(player1, new ThunderousOrator());
        addReadyCreature(player1, new VampireNighthawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's keyword does not qualify")
    void opponentKeywordDoesNotQualify() {
        Permanent orator = addReadyCreature(player1, new ThunderousOrator());
        addReadyCreature(player2, new VampireNighthawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
