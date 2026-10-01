package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({PlaxcasterFrogling.class, MistralCharger.class, AzoriusSignet.class})
class PlaxcasterFroglingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        Permanent frogling = harness.enterBattlefieldAndReturn(player1, new PlaxcasterFrogling());

        assertThat(frogling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Graft may move a +1/+1 counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent frogling = addFrogling(player1);
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Mistral Charger");
        assertThat(frogling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto an opponent's creature that enters")
    void graftMovesCounterOntoOpponentsEnteringCreature() {
        Permanent frogling = addFrogling(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();

        Permanent charger = findPermanent(player2, "Mistral Charger");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(frogling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent frogling = addFrogling(player1);
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent charger = findPermanent(player1, "Mistral Charger");
        assertThat(frogling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gives a target creature with a +1/+1 counter shroud until end of turn")
    void grantsShroudUntilEndOfTurn() {
        addFrogling(player1);
        Permanent target = addReadyCreature(player1, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        addFrogling(player1);
        Permanent target = addReadyCreature(player1, new MistralCharger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with a +1/+1 counter")
    void cannotTargetNoncreatureWithCounter() {
        addFrogling(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature with a +1/+1 counter")
    void grantsShroudToOpponentsCreature() {
        addFrogling(player1);
        Permanent target = addReadyCreature(player2, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();
    }

    private Permanent addFrogling(Player player) {
        return harness.enterBattlefieldAndReturn(player, new PlaxcasterFrogling());
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
