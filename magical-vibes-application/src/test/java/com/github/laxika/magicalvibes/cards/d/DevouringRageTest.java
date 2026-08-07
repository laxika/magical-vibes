package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevouringRageTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        return bears;
    }

    @Test
    @DisplayName("Gives +3/+0 plus an additional +3/+0 for each Spirit sacrificed")
    void boostsThreePlusThreePerSacrificedSpirit() {
        Permanent bears = addBears();
        Permanent first = new Permanent(new KamiOfOldStone());
        Permanent second = new Permanent(new KamiOfOldStone());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(first, second));

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, bears.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(9);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(11);
        harness.assertInGraveyard(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Sacrificing no Spirits still gives +3/+0")
    void sacrificingNoSpiritsGivesThree() {
        Permanent bears = addBears();

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, bears.getId(), List.of());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addBears();

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Spirit to pay the cost")
    void cannotSacrificeNonSpirit() {
        Permanent bears = addBears();

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, bears.getId(),
                List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent bears = addBears();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, forest.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.getPowerModifier()).isEqualTo(0);
    }
}
