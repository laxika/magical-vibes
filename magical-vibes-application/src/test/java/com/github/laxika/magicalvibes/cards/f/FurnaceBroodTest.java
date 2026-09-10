package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MedicineBag;
import com.github.laxika.magicalvibes.cards.p.PygmyTroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurnaceBrood.class, PygmyTroll.class, MedicineBag.class})
class FurnaceBroodTest extends BaseCardTest {

    @Test
    @DisplayName("The red ability marks a target creature so it can't be regenerated this turn")
    void marksTargetCreature() {
        addCreatureReady(player1, new FurnaceBrood());
        Permanent troll = addCreatureReady(player2, new PygmyTroll());
        troll.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, troll.getId());
        harness.passBothPriorities();

        assertThat(troll.isCantRegenerateThisTurn()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration-prevention mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        addCreatureReady(player1, new FurnaceBrood());
        Permanent troll = addCreatureReady(player2, new PygmyTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, troll.getId());
        harness.passBothPriorities();
        assertThat(troll.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(troll.isCantRegenerateThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new FurnaceBrood());
        Permanent medicineBag = harness.addToBattlefieldAndReturn(player1, new MedicineBag());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, medicineBag.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A regeneration shield cannot save a creature marked by the ability")
    void preventsRegenerationFromSavingCreature() {
        addCreatureReady(player1, new FurnaceBrood());
        Permanent troll = addCreatureReady(player2, new PygmyTroll());
        troll.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, troll.getId());
        harness.passBothPriorities();

        troll.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Pygmy Troll");
        harness.assertInGraveyard(player2, "Pygmy Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }
}
