package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.p.PrincessLucrezia;
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

@CardUsed({SeafarersQuay.class, PrincessLucrezia.class, Johan.class, GrizzlyBears.class})
class SeafarersQuayTest extends BaseCardTest {

    @Test
    @DisplayName("Blue legendary creatures can band with other legendary creatures")
    void blueLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        Permanent blueLegendary = addReady(player1, new PrincessLucrezia());
        Permanent otherLegendary = addReady(player1, new Johan());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(blueLegendary.getBandId()).isNotNull();
        assertThat(blueLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new SeafarersQuay());
        addReady(player1, new PrincessLucrezia());
        addReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void declareBand(Player player, List<Integer> attackers, List<List<Integer>> bands) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player, attackers, null, bands));
    }
}
