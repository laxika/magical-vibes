package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.cards.s.StringOfDisappearances;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LancerSliver.class, MetallicSliver.class, GrizzlyBears.class, UniversalAutomaton.class, StringOfDisappearances.class})
class LancerSliverTest extends BaseCardTest {

    @Test
    void grantsFirstStrikeToItselfAndOtherSliversYouControl() {
        Permanent lancer = addCreatureReady(player1, new LancerSliver());
        Permanent otherSliver = addCreatureReady(player1, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doesNotGrantFirstStrikeToNonSliversOrOpponentsSlivers() {
        addCreatureReady(player1, new LancerSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSliver = addCreatureReady(player2, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void grantsFirstStrikeToChangelingsIncludingThoseEnteringLater() {
        Permanent earlierChangeling = addCreatureReady(player1, new UniversalAutomaton());
        assertThat(gqs.hasKeyword(gd, earlierChangeling, Keyword.FIRST_STRIKE)).isFalse();

        addCreatureReady(player1, new LancerSliver());
        Permanent laterChangeling = addCreatureReady(player1, new UniversalAutomaton());
        Permanent opposingChangeling = addCreatureReady(player2, new UniversalAutomaton());

        assertThat(gqs.hasKeyword(gd, earlierChangeling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterChangeling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingChangeling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void firstStrikeEndsWhenLancerLeavesTheBattlefield() {
        Permanent lancer = addCreatureReady(player1, new LancerSliver());
        Permanent changeling = addCreatureReady(player1, new UniversalAutomaton());
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
        harness.setHand(player1, List.of(new StringOfDisappearances()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, lancer.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Lancer Sliver");
        harness.assertInHand(player1, "Lancer Sliver");
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isFalse();
    }
}
