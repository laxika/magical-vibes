package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WojekHalberdiers.class, GrizzlyBears.class, RapidHybridization.class})
class WojekHalberdiersTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants first strike to Wojek Halberdiers only")
    void battalionGrantsFirstStrike() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(otherAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike grant wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }
    @Test
    @DisplayName("Attacking alone does not trigger battalion despite other creatures being present")
    void attackingAloneDoesNotTriggerBattalion() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new WojekHalberdiers());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Nonattacking Halberdiers do not gain first strike when three others attack")
    void nonattackingHalberdiersDoNotGainFirstStrike() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        Permanent firstAttacker = addCreatureReady(player1, new WojekHalberdiers());
        Permanent secondAttacker = addCreatureReady(player1, new WojekHalberdiers());
        Permanent thirdAttacker = addCreatureReady(player1, new WojekHalberdiers());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(firstAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(secondAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(thirdAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Battalion still grants first strike after another attacker leaves the battlefield")
    void battalionDoesNotRecheckAttackerCountOnResolution() {
        Permanent halberdiers = addCreatureReady(player1, new WojekHalberdiers());
        Permanent otherAttacker = addCreatureReady(player1, new WojekHalberdiers());
        addCreatureReady(player1, new WojekHalberdiers());
        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(gd.stack).isNotEmpty();
        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.castAndResolveInstant(player2, 0, otherAttacker.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        resolveAllTriggers();

        assertThat(halberdiers.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }
}
