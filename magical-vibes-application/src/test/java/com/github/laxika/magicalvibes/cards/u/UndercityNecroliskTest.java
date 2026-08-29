package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UndercityNecroliskTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Undercity Necrolisk and grants menace")
    void sacrificingAnotherCreaturePutsCounterAndGrantsMenace() {
        Permanent necrolisk = addReadyNecrolisk(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Undercity Necrolisk");
    }

    @Test
    @DisplayName("Menace wears off at the end of the turn while the counter remains")
    void menaceWearsOffAtEndOfTurn() {
        Permanent necrolisk = addReadyNecrolisk(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Undercity Necrolisk itself")
    void activatedAbilityRequiresAnotherCreature() {
        addReadyNecrolisk(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyNecrolisk(Player player) {
        Permanent necrolisk = new Permanent(new UndercityNecrolisk());
        necrolisk.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(necrolisk);
        return necrolisk;
    }
}
