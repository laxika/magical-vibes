package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CaldaiaStrongarm.class, Goldhound.class})
class CaldaiaStrongarmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts two +1/+1 counters on a target creature")
    void etbPutsTwoCountersOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Goldhound());

        harness.setHand(player1, List.of(new CaldaiaStrongarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Normal cast does not grant haste or sacrifice at the next end step")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new CaldaiaStrongarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent strongarm = findPermanent(player1, "Caldaia Strongarm");
        harness.handlePermanentChosen(player1, strongarm.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, strongarm, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strongarm);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new CaldaiaStrongarm()));
        harness.setLibrary(player1, List.of(new Goldhound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent strongarm = findPermanent(player1, "Caldaia Strongarm");
        harness.handlePermanentChosen(player1, strongarm.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, strongarm, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strongarm);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Caldaia Strongarm");
        harness.assertInHand(player1, "Goldhound");
    }

    @Test
    @DisplayName("Blitz grants haste before the counter trigger resolves")
    void blitzHasHasteWhileCounterTriggerIsPending() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Goldhound());
        harness.setHand(player1, List.of(new CaldaiaStrongarm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        Permanent strongarm = findPermanent(player1, "Caldaia Strongarm");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, strongarm, Keyword.HASTE)).isTrue();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Losing the counter target does not prevent the blitz sacrifice")
    void blitzSacrificesEvenWhenCounterTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Goldhound());
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new CaldaiaStrongarm()));
        harness.setLibrary(player1, List.of(new Goldhound()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Caldaia Strongarm");
        harness.assertInHand(player1, "Goldhound");
    }

    @Test
    @DisplayName("The counter trigger can target an opponent's creature")
    void countersCanBePutOnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Goldhound());
        harness.setHand(player1, List.of(new CaldaiaStrongarm()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

}
