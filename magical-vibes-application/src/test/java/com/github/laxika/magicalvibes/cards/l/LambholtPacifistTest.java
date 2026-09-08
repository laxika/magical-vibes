package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LambholtPacifistTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a creature with power 4 or greater")
    void cannotAttackWithoutPowerFourCreature() {
        addCreatureReady(player1, new LambholtPacifist());
        addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only an opponent controls a creature with power 4 or greater")
    void cannotAttackWithOnlyOpponentPowerFourCreature() {
        addCreatureReady(player1, new LambholtPacifist());
        addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controller controls a creature with power 4 or greater")
    void canAttackWithPowerFourCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LambholtPacifist());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Transforms to Lambholt Butcher when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtPacifist());
        Permanent pacifist = findPermanent(player1, "Lambholt Pacifist");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTrigger(player1);

        assertThat(pacifist.isTransformed()).isTrue();
        assertThat(pacifist.getCard().getName()).isEqualTo("Lambholt Butcher");
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellWasCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtPacifist());
        Permanent pacifist = findPermanent(player1, "Lambholt Pacifist");
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pacifist.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Lambholt Butcher transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsWereCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtPacifist());
        Permanent pacifist = findPermanent(player1, "Lambholt Pacifist");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTrigger(player1);
        assertThat(pacifist.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeepAndResolveTrigger(player2);

        assertThat(pacifist.isTransformed()).isFalse();
        assertThat(pacifist.getCard().getName()).isEqualTo("Lambholt Pacifist");
    }

    private void advanceToUpkeepAndResolveTrigger(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
