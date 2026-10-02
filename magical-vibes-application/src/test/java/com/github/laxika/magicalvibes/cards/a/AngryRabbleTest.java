package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngryRabble.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class AngryRabbleTest extends BaseCardTest {

    @Test
    void highManaValueSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    void spellWithManaValueLessThanFourDoesNotDealDamage() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void activatedAbilityPutsTwoPlusOnePlusOneCountersOnAngryRabble() {
        Permanent rabble = addCreatureReady(player1, new AngryRabble());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rabble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void spellWithManaValueExactlyFourTriggersBeforeSpellResolves() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife - 1);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void opponentsHighManaValueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void activatedAbilityCanBeRepeatedWhileTappedAndSummoningSick() {
        Permanent rabble = harness.addToBattlefieldAndReturn(player1, new AngryRabble());
        rabble.setTapped(true);
        rabble.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rabble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(rabble.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityCannotBeUsedDuringCombat() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityCannotBeUsedDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityCannotBeUsedWithNonemptyStack() {
        harness.addToBattlefield(player1, new AngryRabble());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();
    }
}
