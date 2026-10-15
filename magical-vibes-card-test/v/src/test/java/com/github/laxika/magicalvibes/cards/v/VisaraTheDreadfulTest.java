package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisaraTheDreadful.class, ElvishWarrior.class, Island.class})
class VisaraTheDreadfulTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature without allowing regeneration")
    void destroysTargetCreatureWithoutRegeneration() {
        addCreatureReady(player1, new VisaraTheDreadful());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        target.setRegenerationShield(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Visara the Dreadful");
        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new VisaraTheDreadful());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new VisaraTheDreadful());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a creature its controller controls and taps as a cost")
    void destroysOwnCreatureAndPaysTapCost() {
        Permanent visara = addCreatureReady(player1, new VisaraTheDreadful());
        Permanent target = addCreatureReady(player1, new ElvishWarrior());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(visara.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Elvish Warrior");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Visara the Dreadful");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent visara = addCreatureReady(player1, new VisaraTheDreadful());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        visara.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player2, "Elvish Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent visara = addCreatureReady(player1, new VisaraTheDreadful());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        visara.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(visara.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Can target itself")
    void canDestroyItself() {
        Permanent visara = addCreatureReady(player1, new VisaraTheDreadful());

        harness.activateAbility(player1, 0, null, visara.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Visara the Dreadful");
        harness.assertInGraveyard(player1, "Visara the Dreadful");
    }
}
