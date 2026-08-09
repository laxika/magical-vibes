package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttritionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature destroys target nonblack creature")
    void sacrificesCreatureAndDestroysNonblackCreature() {
        harness.addToBattlefield(player1, new Attrition());
        Permanent fodder = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(fodder);
        Permanent target = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Attrition());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = new Permanent(new MassOfGhouls());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player1, new Attrition());
        Permanent target = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
