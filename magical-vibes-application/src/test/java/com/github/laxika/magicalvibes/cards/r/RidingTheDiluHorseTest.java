package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RidingTheDiluHorse.class, ForestBear.class, Forest.class})
class RidingTheDiluHorseTest extends BaseCardTest {

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new ForestBear());
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new RidingTheDiluHorse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving gives +2/+2 and horsemanship to the target creature")
    void resolvesBoostAndHorsemanship() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void targetsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new ForestBear());
        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Boost and horsemanship last indefinitely (survive end of turn)")
    void lastsIndefinitely() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Two copies on the same creature stack additively")
    void copiesStack() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        castOn(target);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Does nothing if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addReadyCreature();
        castOn(target);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Riding the Dilu Horse");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ForestBear()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> castOn(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
