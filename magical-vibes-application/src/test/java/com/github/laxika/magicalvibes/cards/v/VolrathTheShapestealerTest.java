package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathTheShapestealer.class, GrizzlyBears.class, Forest.class})
class VolrathTheShapestealerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on up to one target creature at combat")
    void putsMinusOneCounterAtBeginningOfCombat() {
        addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Becomes a 7/5 copy while retaining its activated ability")
    void becomesCopyWithExceptionAndRetainedAbility() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(volrath.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(volrath.getCard().getPower()).isEqualTo(7);
        assertThat(volrath.getCard().getToughness()).isEqualTo(5);
        assertThat(volrath.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Copy reverts at the beginning of Volrath's next turn")
    void copyRevertsAtNextTurn() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(volrath.getCard().getName()).isEqualTo("Volrath, the Shapestealer");
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature permanent")
    void activatedAbilityRejectsNoncreatureTarget() {
        addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
