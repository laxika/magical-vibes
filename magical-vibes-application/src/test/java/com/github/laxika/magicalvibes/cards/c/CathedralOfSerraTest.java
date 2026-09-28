package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdunOakenshield;
import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.t.TetsuoUmezawa;
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

@CardUsed({CathedralOfSerra.class, AdunOakenshield.class, Johan.class, TetsuoUmezawa.class})
class CathedralOfSerraTest extends BaseCardTest {

    @Test
    @DisplayName("White legendary creatures can band with other legendary creatures")
    void whiteLegendaryCanBandWithOtherLegendary() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        Permanent whiteLegendary = addCreatureReady(player1, new Johan());
        Permanent otherLegendary = addCreatureReady(player1, new AdunOakenshield());

        declareBand(player1, List.of(1, 2));

        assertThat(whiteLegendary.getBandId()).isNotNull();
        assertThat(whiteLegendary.getBandId()).isEqualTo(otherLegendary.getBandId());
    }

    @Test
    @DisplayName("A band without a white legendary creature is rejected")
    void bandWithoutWhiteLegendaryCreatureIsRejected() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        addCreatureReady(player1, new AdunOakenshield());
        addCreatureReady(player1, new TetsuoUmezawa());

        beginAttackDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player1, List.of(1, 2), null, List.of(List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("The ability does not apply to white legendary creatures controlled by an opponent")
    void abilityDoesNotApplyToOpponentsCreatures() {
        harness.addToBattlefield(player1, new CathedralOfSerra());
        addCreatureReady(player2, new Johan());
        addCreatureReady(player2, new AdunOakenshield());

        beginAttackDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    private void beginAttackDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareBand(Player player, List<Integer> attackers) {
        beginAttackDeclaration(player);
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player, attackers, null, List.of(attackers)));
    }
}
