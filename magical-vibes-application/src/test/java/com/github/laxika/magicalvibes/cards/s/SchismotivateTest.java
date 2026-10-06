package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
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

@CardUsed({Schismotivate.class, Gristleback.class, IzzetSignet.class})
class SchismotivateTest extends BaseCardTest {

    @Test
    @DisplayName("Gives one target creature +4/+0 and another -4/-0")
    void appliesBothModifiers() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        prepare();

        harness.castAndResolveInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));

        assertThat(boosted.getEffectivePower()).isEqualTo(6);
        assertThat(boosted.getEffectiveToughness()).isEqualTo(2);
        assertThat(weakened.getEffectivePower()).isEqualTo(-2);
        assertThat(weakened.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both modifiers wear off at cleanup")
    void modifiersWearOffAtCleanup() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        prepare();

        harness.castAndResolveInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        assertThat(boosted.getEffectivePower()).isEqualTo(6);
        assertThat(weakened.getEffectivePower()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(boosted.getEffectivePower()).isEqualTo(2);
        assertThat(weakened.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Still modifies the surviving target when the other target leaves")
    void resolvesWithOneLegalTarget() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        prepare();

        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(boosted);
        harness.passBothPriorities();

        assertThat(weakened.getEffectivePower()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void requiresAnotherCreatureAsSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        prepare();

        assertThatThrownBy(() -> harness.castInstant(
                        player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetSignet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                        List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still boosts the first target when the second target leaves")
    void resolvesWithOnlyFirstTargetLegal() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        prepare();

        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(weakened);
        harness.passBothPriorities();

        assertThat(boosted.getEffectivePower()).isEqualTo(6);
        assertThat(boosted.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not resolve when both targets leave")
    void doesNotResolveWithNoLegalTargets() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new Gristleback());
        prepare();

        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(boosted);
        gd.playerBattlefields.get(player2.getId()).remove(weakened);
        harness.passBothPriorities();

        assertThat(boosted.getEffectivePower()).isEqualTo(2);
        assertThat(weakened.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Schismotivate");
    }

    @Test
    @DisplayName("Cannot choose a noncreature as the second target")
    void cannotTargetNonCreatureAsSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                        List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two targets to cast")
    void cannotCastWithOnlyOneTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gristleback());
        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepare() {
        harness.setHand(player1, List.of(new Schismotivate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
