package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Attrition.class, GoliathBeetle.class, RavenousRats.class})
class AttritionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature destroys target nonblack creature")
    void sacrificesCreatureAndDestroysNonblackCreature() {
        harness.addToBattlefield(player1, new Attrition());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
        harness.assertInGraveyard(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Attrition());
        harness.addToBattlefield(player1, new GoliathBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousRats());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Attrition());
        harness.addToBattlefield(player1, new GoliathBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Attrition());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a nonblack creature controlled by its controller")
    void canTargetOwnNonblackCreature() {
        harness.addToBattlefield(player1, new Attrition());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player1, new Attrition());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
