package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GatstafArsonists;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LambholtPacifist.class, DevilthornFox.class, GatstafArsonists.class})
class LambholtPacifistTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a creature with power 4 or greater")
    void cannotAttackWithoutPowerFourCreature() {
        addCreatureReady(player1, new LambholtPacifist());
        addCreatureReady(player1, new DevilthornFox());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only an opponent controls a creature with power 4 or greater")
    void cannotAttackWithOnlyOpponentPowerFourCreature() {
        addCreatureReady(player1, new LambholtPacifist());
        addCreatureReady(player2, new GatstafArsonists());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controller controls a creature with power 4 or greater")
    void canAttackWithPowerFourCreature() {
        addCreatureReady(player1, new LambholtPacifist());
        addCreatureReady(player1, new GatstafArsonists());

        declareAttackers(player1, List.of(0));

        assertThat(findPermanent(player1, "Lambholt Pacifist").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Transforms to Lambholt Butcher when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtPacifist());
        Permanent pacifist = findPermanent(player1, "Lambholt Pacifist");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(pacifist.isTransformed()).isTrue();
        assertThat(pacifist.getCard().getName()).isEqualTo("Lambholt Butcher");
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellWasCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtPacifist());
        Permanent pacifist = findPermanent(player1, "Lambholt Pacifist");
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(pacifist.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Lambholt Butcher transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsWereCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtPacifist());
        Permanent pacifist = findPermanent(player1, "Lambholt Pacifist");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(pacifist.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(pacifist.isTransformed()).isFalse();
        assertThat(pacifist.getCard().getName()).isEqualTo("Lambholt Pacifist");
    }

    @Test
    void canAttackWhenItsOwnPowerIsFour() {
        Permanent pacifist = addCreatureReady(player1, new LambholtPacifist());
        pacifist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(pacifist.isAttacking()).isTrue();
    }

    @Test
    void transformsOnOpponentsUpkeep() {
        Permanent pacifist = addCreatureReady(player1, new LambholtPacifist());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(pacifist.isTransformed()).isTrue();
    }

    @Test
    void butcherDoesNotTransformWhenEachPlayerCastOneSpell() {
        Permanent pacifist = addCreatureReady(player1, new LambholtPacifist());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(pacifist.isTransformed()).isTrue();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(pacifist.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void butcherCanAttackWithoutAnotherCreature() {
        Permanent pacifist = addCreatureReady(player1, new LambholtPacifist());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(pacifist.isTransformed()).isTrue();

        declareAttackers(player1, List.of(0));

        assertThat(pacifist.isAttacking()).isTrue();
    }
}
