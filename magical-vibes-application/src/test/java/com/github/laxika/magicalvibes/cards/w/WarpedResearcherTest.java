package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.k.KeeneyeAven;
import com.github.laxika.magicalvibes.cards.r.RiptideMangler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarpedResearcher.class, KeeneyeAven.class, FugitiveWizard.class, RiptideMangler.class})
class WarpedResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives Warped Researcher flying and shroud")
    void cyclingGrantsFlyingAndShroud() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, researcher, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, researcher, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("An opponent cycling a card gives Warped Researcher flying and shroud")
    void opponentsCyclingGrantsFlyingAndShroud() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        setUpCycling(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, researcher, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, researcher, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, researcher, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, researcher, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, researcher, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, researcher, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Granted shroud prevents targeted abilities from targeting Warped Researcher")
    void grantedShroudPreventsAbilityTargeting() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new WarpedResearcher());
        Permanent mangler = harness.addToBattlefieldAndReturn(player1, new RiptideMangler());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mangler),
                null,
                researcher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private void setUpCycling(Player player) {
        harness.setHand(player, List.of(new KeeneyeAven()));
        harness.setLibrary(player, List.of(new FugitiveWizard()));
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
