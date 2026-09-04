package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArnjlotsAscent.class, BalduvianBears.class, Island.class})
class ArnjlotsAscentTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Arnjlot's Ascent")
    void paysCumulativeUpkeep() {
        Permanent ascent = harness.addToBattlefieldAndReturn(player1, new ArnjlotsAscent());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(ascent.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ascent);
    }

    @Test
    @DisplayName("Cumulative upkeep costs two blue mana on the second upkeep")
    void cumulativeUpkeepCostIncreases() {
        Permanent ascent = harness.addToBattlefieldAndReturn(player1, new ArnjlotsAscent());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ascent.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ascent);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Arnjlot's Ascent")
    void declineSacrifices() {
        Permanent ascent = harness.addToBattlefieldAndReturn(player1, new ArnjlotsAscent());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ascent);
        harness.assertInGraveyard(player1, "Arnjlot's Ascent");
    }

    @Test
    @DisplayName("Accepting cumulative upkeep without enough mana sacrifices Arnjlot's Ascent")
    void cannotPayCumulativeUpkeep() {
        Permanent ascent = harness.addToBattlefieldAndReturn(player1, new ArnjlotsAscent());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ascent);
        harness.assertInGraveyard(player1, "Arnjlot's Ascent");
    }

    @Test
    @DisplayName("{1} grants target creature flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        harness.addToBattlefield(player1, new ArnjlotsAscent());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{1} can grant flying to a creature an opponent controls")
    void grantsFlyingToOpponentCreature() {
        harness.addToBattlefield(player1, new ArnjlotsAscent());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("{1} cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ArnjlotsAscent());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new ArnjlotsAscent());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
