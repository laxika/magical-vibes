package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skitterskin.class, GrizzlyBears.class})
class SkitterskinTest extends BaseCardTest {

    @Test
    @DisplayName("Skitterskin cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent skitterskin = new Permanent(new Skitterskin());
        skitterskin.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(skitterskin);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Regeneration requires another colorless creature")
    void regenerationRequiresAnotherColorlessCreature() {
        addCreatureReady(player1, new Skitterskin());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another colorless creature enables regeneration")
    void anotherColorlessCreatureEnablesRegeneration() {
        Permanent skitterskin = addCreatureReady(player1, new Skitterskin());
        addCreatureReady(player1, new Skitterskin());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skitterskin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A colored creature does not enable regeneration")
    void coloredCreatureDoesNotEnableRegeneration() {
        addCreatureReady(player1, new Skitterskin());
        addCreatureReady(player1, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
