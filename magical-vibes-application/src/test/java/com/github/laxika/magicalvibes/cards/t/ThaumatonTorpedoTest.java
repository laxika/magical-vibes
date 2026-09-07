package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExtinguisherBattleship;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThaumatonTorpedo.class, ExtinguisherBattleship.class, Forest.class, GrizzlyBears.class})
class ThaumatonTorpedoTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonland permanent and sacrifices itself")
    void destroysNonlandPermanent() {
        Permanent torpedo = harness.addToBattlefieldAndReturn(player1, new ThaumatonTorpedo());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, battlefieldIndex(torpedo), null, target.getId());
        harness.assertInGraveyard(player1, "Thaumaton Torpedo");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs three mana after attacking with a Spacecraft")
    void costsThreeAfterAttackingWithSpacecraft() {
        Permanent torpedo = harness.addToBattlefieldAndReturn(player1, new ThaumatonTorpedo());
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new ExtinguisherBattleship());
        spacecraft.setCounterCount(CounterType.CHARGE, 5);
        spacecraft.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(battlefieldIndex(spacecraft)));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(torpedo), null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent torpedo = harness.addToBattlefieldAndReturn(player1, new ThaumatonTorpedo());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(
                        player1, battlefieldIndex(torpedo), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
