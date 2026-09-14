package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Implode;
import com.github.laxika.magicalvibes.cards.r.RithsCharm;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudBlessing.class, TerminalMoraine.class, RithsCharm.class, SeaSnidd.class, Implode.class})
class SkyshroudBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("All lands gain shroud and the caster draws a card")
    void allLandsGainShroudAndDrawsCard() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        harness.setLibrary(player1, List.of(new Implode()));

        harness.castFromHand(player1, new SkyshroudBlessing(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentLand, Keyword.SHROUD)).isTrue();
        harness.assertInHand(player1, "Implode");
    }

    @Test
    @DisplayName("Shroud prevents targeting a land")
    void shroudPreventsTargetingALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        harness.setLibrary(player1, List.of(new Implode()));

        harness.castFromHand(player1, new SkyshroudBlessing(), "{1}{G}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new RithsCharm()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents targeting a land with an ability")
    void shroudPreventsTargetingALandWithAnAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        Permanent seaSnidd = addCreatureReady(player2, new SeaSnidd());
        harness.setLibrary(player1, List.of(new Implode()));

        harness.castFromHand(player1, new SkyshroudBlessing(), "{1}{G}");
        harness.passBothPriorities();

        int seaSniddIndex = gd.playerBattlefields.get(player2.getId()).indexOf(seaSnidd);
        assertThatThrownBy(() -> harness.activateAbility(player2, seaSniddIndex, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("A land entering after resolution does not gain shroud")
    void landEnteringAfterResolutionDoesNotGainShroud() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        harness.setLibrary(player1, List.of(new Implode()));

        harness.castFromHand(player1, new SkyshroudBlessing(), "{1}{G}");
        harness.passBothPriorities();

        Permanent laterLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());

        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        harness.setLibrary(player1, List.of(new Implode()));

        harness.castFromHand(player1, new SkyshroudBlessing(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, land, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, land, Keyword.SHROUD)).isFalse();
    }
}
