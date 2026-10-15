package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.w.WingedSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalonSliver.class, WingedSliver.class, LowlandGiant.class})
class TalonSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Talon Sliver grants itself first strike (it is a Sliver)")
    void grantsSelfFirstStrike() {
        Permanent sliver = addCreatureReady(player1, new TalonSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to another Sliver you control")
    void grantsFirstStrikeToOtherSliver() {
        addCreatureReady(player1, new TalonSliver());
        Permanent otherSliver = addCreatureReady(player1, new WingedSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to an opponent's Sliver too")
    void grantsFirstStrikeToOpponentSliver() {
        addCreatureReady(player1, new TalonSliver());
        Permanent opponentSliver = addCreatureReady(player2, new WingedSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant first strike to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new TalonSliver());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());

        assertThat(gqs.hasKeyword(gd, giant, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Slivers lose granted first strike when Talon Sliver dies")
    void losesFirstStrikeWhenSourceDies() {
        Permanent source = addCreatureReady(player1, new TalonSliver());
        Permanent friendlySliver = addCreatureReady(player1, new WingedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());

        assertThat(gqs.hasKeyword(gd, friendlySliver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FIRST_STRIKE)).isTrue();

        source.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Talon Sliver");
        assertThat(gqs.hasKeyword(gd, friendlySliver, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("One surviving Talon Sliver keeps first strike active for all Slivers")
    void survivingSourceKeepsFirstStrike() {
        Permanent firstSource = addCreatureReady(player1, new TalonSliver());
        Permanent secondSource = addCreatureReady(player2, new TalonSliver());
        Permanent sliver = addCreatureReady(player1, new WingedSliver());

        firstSource.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Talon Sliver");
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FIRST_STRIKE)).isTrue();

        secondSource.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Talon Sliver");
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FIRST_STRIKE)).isFalse();
    }
}
