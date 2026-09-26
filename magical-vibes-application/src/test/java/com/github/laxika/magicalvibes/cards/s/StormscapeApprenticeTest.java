package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormscapeApprentice.class, RagingKavu.class, Forest.class})
class StormscapeApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: taps target creature")
    void tapsTargetCreature() {
        Permanent apprentice = addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{B}, {T}: causes target player to lose 1 life")
    void targetPlayerLosesLife() {
        Permanent apprentice = addReadyApprentice();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("{B}, {T}: can target its controller")
    void targetPlayerMayBeController() {
        Permanent apprentice = addReadyApprentice();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The tap cost prevents activating another ability while tapped")
    void tapCostPreventsAnotherActivationWhileTapped() {
        Permanent apprentice = addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent is already tapped");
    }

    @Test
    @DisplayName("The tap ability cannot target a non-creature")
    void tapAbilityRejectsNonCreatureTarget() {
        addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyApprentice() {
        return addCreatureReady(player1, new StormscapeApprentice());
    }
}
