package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BrittleEffigy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiverOfRunes.class, GrizzlyBears.class, BrittleEffigy.class})
class GiverOfRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping grants chosen-color protection to another creature you control")
    void grantsChosenColorProtection() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Tapping can grant protection from colorless")
    void grantsProtectionFromColorless() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, null)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, target, new Permanent(new BrittleEffigy()))).isTrue();
    }

    @Test
    @DisplayName("The granted protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The ability only targets another creature you control")
    void restrictsTargets() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent noncreature = new Permanent(new BrittleEffigy());
        gd.playerBattlefields.get(player1.getId()).add(noncreature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, giver.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
