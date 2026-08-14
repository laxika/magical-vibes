package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViciousBetrayalTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Gives +2/+2 for each creature sacrificed")
    void boostsForEachCreatureSacrificed() {
        Permanent target = new Permanent(new GrizzlyBears());
        Permanent firstSacrifice = new Permanent(new GrizzlyBears());
        Permanent secondSacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(firstSacrifice, secondSacrifice));

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, target.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing no creatures gives +0/+0")
    void sacrificingNoCreaturesGivesNoBoost() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = new Permanent(new GrizzlyBears());
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreature() {
        Permanent target = new Permanent(new GrizzlyBears());
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.playerBattlefields.get(player1.getId()).add(forest);

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, target.getId(),
                List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, forest.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
