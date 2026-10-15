package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrCharge.class, CoralMerfolk.class})
class ZephyrChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to target creature")
    void grantsFlying() {
        harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability can target a creature an opponent controls")
    void grantsFlyingToOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player2, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated repeatedly (no tap cost)")
    void noTapCost() {
        harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        Permanent first = addCreatureReady(player1, new CoralMerfolk());
        Permanent second = addCreatureReady(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player1, new CoralMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability requires blue mana even when two generic mana are available")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature enchantment")
    void cannotTargetNoncreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(source.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Activated ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability does not grant flying to a creature that leaves and returns")
    void returningCreatureIsANewObject() {
        harness.addToBattlefield(player1, new ZephyrCharge());
        Permanent target = addCreatureReady(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, target.getCard());
        harness.passBothPriorities();

        assertThat(returned.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
