package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.FencingAce;
import com.github.laxika.magicalvibes.cards.f.FathomFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GarruksCompanion;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderousOrator.class, VampireNighthawk.class, WhiteKnight.class, FencingAce.class,
        DarksteelMyr.class, FathomFleetCaptain.class, GarruksCompanion.class})
class ThunderousOratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gains each matching keyword when the attack trigger resolves")
    void gainsMatchingKeywordsAtResolution() {
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());

        declareAttackers(player1, List.of(0));
        addCreatureReady(player1, new VampireNighthawk());
        addCreatureReady(player1, new WhiteKnight());
        addCreatureReady(player1, new FencingAce());
        addCreatureReady(player1, new DarksteelMyr());
        addCreatureReady(player1, new FathomFleetCaptain());
        addCreatureReady(player1, new GarruksCompanion());
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
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());
        addCreatureReady(player1, new VampireNighthawk());

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
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());
        addCreatureReady(player2, new VampireNighthawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A matching creature leaving before resolution does not grant its keywords")
    void matchingCreatureMustStillBeControlledAtResolution() {
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());
        Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(nighthawk);
        gd.playerGraveyards.get(player1.getId()).add(nighthawk.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords persist after the matching creature leaves")
    void grantedKeywordsPersistWithoutMatchingCreature() {
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());
        Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(nighthawk);
        gd.playerGraveyards.get(player1.getId()).add(nighthawk.getCard());

        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Double strike does not also grant flying or first strike")
    void grantsOnlyTheMatchingKeyword() {
        Permanent orator = addCreatureReady(player1, new ThunderousOrator());
        addCreatureReady(player1, new FencingAce());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, orator, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, orator, Keyword.FLYING)).isFalse();
    }
}
