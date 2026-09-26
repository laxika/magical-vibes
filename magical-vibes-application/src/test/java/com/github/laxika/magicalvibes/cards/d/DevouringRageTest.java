package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevouringRage.class, KamiOfOldStone.class, Forest.class})
class DevouringRageTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addTargetCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KamiOfOldStone());
    }

    @Test
    @DisplayName("Gives +3/+0 plus an additional +3/+0 for each Spirit sacrificed")
    void boostsThreePlusThreePerSacrificedSpirit() {
        Permanent target = addTargetCreature(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castInstantWithSacrifices(player1, 0, target.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(9);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(10);
        harness.assertInGraveyard(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Sacrificing no Spirits still gives +3/+0")
    void sacrificingNoSpiritsGivesThree() {
        Permanent target = addTargetCreature(player1);

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castInstantWithSacrifices(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addTargetCreature(player1);

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castInstantWithSacrifices(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = addTargetCreature(player2);

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        harness.castInstantWithSacrifices(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot sacrifice a Spirit an opponent controls")
    void cannotSacrificeOpponentSpirit() {
        Permanent target = addTargetCreature(player2);

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, target.getId(),
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Cannot choose the same Spirit twice for the additional cost")
    void cannotSacrificeSameSpiritTwice() {
        Permanent target = addTargetCreature(player1);

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, target.getId(),
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getPowerModifier()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Requires a target creature")
    void cannotCastWithoutTargetCreature() {
        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Devouring Rage");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Spirit to pay the cost")
    void cannotSacrificeNonSpirit() {
        Permanent target = addTargetCreature(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, target.getId(),
                List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getPowerModifier()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new DevouringRage()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(player1, 0, forest.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
    }
}
