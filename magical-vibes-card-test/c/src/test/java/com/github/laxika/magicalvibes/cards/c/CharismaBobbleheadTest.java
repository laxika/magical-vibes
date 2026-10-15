package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CharismaBobblehead.class)
class CharismaBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new CharismaBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
    }

    @Test
    void createsOneSoldierPerBobblehead() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
    }

    @Test
    void soldierAbilityPaysTapAndManaCostsBeforeResolution() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(bobblehead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void tappedBobbleheadCannotCreateSoldiers() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new CharismaBobblehead());
        bobblehead.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentBobbleheadsDoNotIncreaseSoldierCount() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addToBattlefield(player2, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    void bobbleheadsAreCountedAtResolution() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
    }

    @Test
    void noSoldiersAreCreatedIfAllBobbleheadsLeaveBeforeResolution() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    void soldierAbilityCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void soldierAbilityCannotBeActivatedOnOpponentsTurn() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void soldierAbilityCannotBeActivatedWithNonemptyStack() {
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addToBattlefield(player1, new CharismaBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}

