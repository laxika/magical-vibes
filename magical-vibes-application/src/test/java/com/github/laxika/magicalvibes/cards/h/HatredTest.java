package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hatred.class, RagingGoblin.class, Spellbook.class})
class HatredTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X life and gives target creature +X/+0")
    void paysLifeAndBoostsPowerOnly() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 4, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(goblin.getPowerModifier()).isEqualTo(4);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getEffectivePower()).isEqualTo(5);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 2, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(8);
        assertThat(goblin.getPowerModifier()).isEqualTo(2);
        assertThat(goblin.getEffectivePower()).isEqualTo(3);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("X=0 pays no life and gives no power")
    void zeroXPaysNoLifeAndDoesNotBoost() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 0, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be cast when X life cannot be paid")
    void cannotPayMoreLifeThanAvailable() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 3);
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 4, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Hatred()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 3, goblin.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new Hatred()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
