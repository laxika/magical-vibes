package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BartelRuneaxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Johan;
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

@CardUsed({MountainStronghold.class, BartelRuneaxe.class, Johan.class, GrizzlyBears.class})
class MountainStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("Red legendary creatures can band with other legendary creatures")
    void redLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new MountainStronghold());
        Permanent redLegendary = addReady(player1, new BartelRuneaxe());
        Permanent otherLegendary = addReady(player1, new Johan());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(redLegendary.getBandId()).isNotNull();
        assertThat(redLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band with a nonlegendary creature is rejected")
    void bandWithNonlegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new MountainStronghold());
        addReady(player1, new BartelRuneaxe());
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
