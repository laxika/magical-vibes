package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OrazcaFrillback;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TetzimocPrimalDeath.class, OrazcaFrillback.class, TravelersAmulet.class})
class TetzimocPrimalDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Its hand ability reveals the card and puts a prey counter on a creature")
    void putsPreyCounterFromHand() {
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PREY)).isEqualTo(1);
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability destroys marked opposing creatures only")
    void destroysMarkedOpposingCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new OrazcaFrillback());
        ownBears.setCounterCount(CounterType.PREY, 1);
        Permanent markedOpponent = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());
        markedOpponent.setCounterCount(CounterType.PREY, 1);
        Permanent unmarkedOpponent = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TetzimocPrimalDeath(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(markedOpponent).contains(unmarkedOpponent);
    }

    @Test
    @DisplayName("Its hand ability can be activated only during its controller's turn")
    void handAbilityRequiresItsControllersTurn() {
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This ability can only be activated during your turn");
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }

    @Test
    @DisplayName("The same card can mark your own creature repeatedly during combat")
    void repeatedlyMarksOwnCreatureDuringCombat() {
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OrazcaFrillback());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.activateHandAbility(player1, 0, creature.getId());
        assertThat(creature.getCounterCount(CounterType.PREY)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PREY)).isEqualTo(2);
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }

    @Test
    @DisplayName("Another Tetzimoc can mark a creature in response to the enter trigger")
    void destroysCreatureMarkedWhileEnterTriggerIsOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TetzimocPrimalDeath(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PREY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Orazca Frillback");
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }

    @Test
    @DisplayName("Marked noncreature permanents survive the enter trigger")
    void sparesMarkedNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TravelersAmulet());
        artifact.setCounterCount(CounterType.PREY, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OrazcaFrillback());
        creature.setCounterCount(CounterType.PREY, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TetzimocPrimalDeath(), "{4}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact).doesNotContain(creature);
        assertThat(artifact.getCounterCount(CounterType.PREY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The hand ability cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TravelersAmulet());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.getCounterCount(CounterType.PREY)).isZero();
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }
}
