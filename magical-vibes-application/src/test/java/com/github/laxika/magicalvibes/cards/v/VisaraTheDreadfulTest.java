package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
