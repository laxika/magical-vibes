package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdunOakenshield;
import com.github.laxika.magicalvibes.cards.a.AyumiTheLastVisitor;
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

@CardUsed({CathedralOfSerra.class, AdunOakenshield.class, AyumiTheLastVisitor.class, Johan.class})
class CathedralOfSerraTest extends BaseCardTest {

    @Test
    @DisplayName("White legendary creatures can band with other legendary creatures")
    void whiteLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        Permanent whiteLegendary = addReady(player1, new Johan());
        Permanent otherLegendary = addReady(player1, new AdunOakenshield());

        declareBand(player1, List.of(1, 2), List.of(List.of(1, 2)));

        assertThat(whiteLegendary.getBandId()).isNotNull();
        assertThat(whiteLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band without a white legendary creature is rejected")
    void bandWithoutWhiteLegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        addReady(player1, new AdunOakenshield());
        addReady(player1, new AyumiTheLastVisitor());

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
