package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianRiftcutter.class, Forest.class, GhostQuarter.class, GrizzlyBears.class})
class BarbarianRiftcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself to destroy a target land")
    void sacrificesItselfToDestroyTargetLand() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barbarian Riftcutter");
        harness.assertInGraveyard(player1, "Barbarian Riftcutter");
        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Can destroy a basic land")
    void canDestroyBasicLand() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new BarbarianRiftcutter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
