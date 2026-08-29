package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AshioksSkulker.class)
class AshioksSkulkerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes Ashiok's Skulker unblockable this turn")
    void makesSelfUnblockable() {
        Permanent skulker = addCreatureReady(player1, new AshioksSkulker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skulker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        Permanent skulker = addCreatureReady(player1, new AshioksSkulker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skulker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability does not tap Ashiok's Skulker")
    void activationDoesNotTap() {
        Permanent skulker = addCreatureReady(player1, new AshioksSkulker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(skulker.isTapped()).isFalse();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
