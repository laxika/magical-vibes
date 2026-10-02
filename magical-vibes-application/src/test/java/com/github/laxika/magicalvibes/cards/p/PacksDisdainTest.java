package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GameTrailChangeling;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
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

@CardUsed({PacksDisdain.class, Bitterblossom.class, ElvishWarrior.class,
        GameTrailChangeling.class, IndomitableAncients.class})
class PacksDisdainTest extends BaseCardTest {

    private void castAt(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new PacksDisdain()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target gets -1/-1 for each permanent of the chosen type you control")
    void minusOneMinusOnePerChosenType() {
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent target = addCreatureReady(player2, new IndomitableAncients());

        castAt(player1, target);
        harness.handleListChoice(player1, "ELF");

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getEffectivePower()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        addCreatureReady(player1, new GameTrailChangeling());
        Permanent target = addCreatureReady(player2, new IndomitableAncients());

        castAt(player1, target);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("A noncreature permanent with the chosen type is counted")
    void nonCreaturePermanentOfChosenTypeCounts() {
        harness.addToBattlefield(player1, new Bitterblossom());
        Permanent target = addCreatureReady(player2, new IndomitableAncients());

        castAt(player1, target);
        harness.handleListChoice(player1, "FAERIE");

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Choosing a type you control none of applies no modifier")
    void chosenTypeYouControlNoneAppliesNothing() {
        Permanent target = addCreatureReady(player2, new IndomitableAncients());

        castAt(player1, target);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player2, new ElvishWarrior());
        Permanent target = addCreatureReady(player2, new IndomitableAncients());

        castAt(player1, target);
        harness.handleListChoice(player1, "ELF");

        assertThat(target.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Modifier wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ElvishWarrior());
        Permanent target = addCreatureReady(player2, new IndomitableAncients());

        castAt(player1, target);
        harness.handleListChoice(player1, "ELF");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new ElvishWarrior());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new Bitterblossom());
        harness.setHand(player1, List.of(new PacksDisdain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
