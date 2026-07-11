package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PacksDisdainTest extends BaseCardTest {

    private void castAt(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new PacksDisdain()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Target gets -1/-1 for each permanent of the chosen type you control")
    void minusOneMinusOnePerChosenType() {
        addCreature(player1, new GrizzlyBears());
        addCreature(player1, new GrizzlyBears());
        Permanent target = addCreature(player2, new HillGiant());

        castAt(player1, target);
        harness.handleListChoice(player1, "BEAR");

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        addCreature(player1, new AvianChangeling());
        Permanent target = addCreature(player2, new HillGiant());

        castAt(player1, target);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Choosing a type you control none of applies no modifier")
    void chosenTypeYouControlNoneAppliesNothing() {
        Permanent target = addCreature(player2, new HillGiant());

        castAt(player1, target);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        addCreature(player1, new GrizzlyBears());
        addCreature(player2, new GrizzlyBears());
        Permanent target = addCreature(player2, new HillGiant());

        castAt(player1, target);
        harness.handleListChoice(player1, "BEAR");

        assertThat(target.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Modifier wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreature(player1, new GrizzlyBears());
        Permanent target = addCreature(player2, new HillGiant());

        castAt(player1, target);
        harness.handleListChoice(player1, "BEAR");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1, new GrizzlyBears());
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new PacksDisdain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
