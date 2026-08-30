package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MasterOfTheHunt;
import com.github.laxika.magicalvibes.cards.u.UrzasEngine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tolaria.class, UrzasEngine.class, MasterOfTheHunt.class})
class TolariaTest extends BaseCardTest {

    @Test
    void addsBlueMana() {
        harness.addToBattlefield(player1, new Tolaria());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void removesBandingAndBandsWithOtherUntilEndOfTurnDuringAnyUpkeep() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new UrzasEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, engine, Keyword.BANDING)).isTrue();

        harness.addToBattlefield(player1, new Tolaria());
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, engine.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, engine, Keyword.BANDING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, engine, Keyword.BANDING)).isTrue();
    }

    @Test
    void removesBandsWithOtherUntilEndOfTurn() {
        harness.addToBattlefield(player1, new MasterOfTheHunt());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent tolaria = harness.addToBattlefieldAndReturn(player1, new Tolaria());
        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);
        harness.activateAbility(player1, tolariaIndex, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");
    }

    @Test
    void cannotActivateCreatureAbilityOutsideUpkeep() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new UrzasEngine());
        harness.addToBattlefield(player1, new Tolaria());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, engine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
