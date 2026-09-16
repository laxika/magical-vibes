package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenevolentBodyguard.class, BorderPatrol.class, KrosanVerge.class})
class BenevolentBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing this creature grants chosen-color protection to a creature you control")
    void sacrificeGrantsChosenColorProtection() {
        addCreatureReady(player1, new BenevolentBodyguard());
        Permanent target = addCreatureReady(player1, new BorderPatrol());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        harness.assertInGraveyard(player1, "Benevolent Bodyguard");
    }

    @Test
    @DisplayName("The granted protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BenevolentBodyguard());
        Permanent target = addCreatureReady(player1, new BorderPatrol());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new BenevolentBodyguard());
        Permanent target = addCreatureReady(player2, new BorderPatrol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new BenevolentBodyguard());
        harness.addToBattlefield(player1, new KrosanVerge());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                null,
                harness.getPermanentId(player1, "Krosan Verge")
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A self-targeted activation fizzles after sacrificing its source")
    void selfTargetBecomesIllegalAfterSacrifice() {
        Permanent bodyguard = addCreatureReady(player1, new BenevolentBodyguard());

        harness.activateAbility(player1, 0, null, bodyguard.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Benevolent Bodyguard");
        harness.assertInGraveyard(player1, "Benevolent Bodyguard");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
