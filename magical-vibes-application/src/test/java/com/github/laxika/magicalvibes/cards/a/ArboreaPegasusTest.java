package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ScaledHerbalist;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArboreaPegasus.class, ScaledHerbalist.class, Plains.class})
class ArboreaPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives any target creature +1/+1 and flying")
    void etbBoostsAndGrantsFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScaledHerbalist());
        harness.setHand(player1, List.of(new ArboreaPegasus()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("ETB boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScaledHerbalist());
        harness.setHand(player1, List.of(new ArboreaPegasus()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new ArboreaPegasus()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pegasus can target itself when it enters an otherwise empty battlefield")
    void canTargetItself() {
        Permanent pegasus = harness.enterBattlefieldAndReturn(player1, new ArboreaPegasus());

        harness.handlePermanentChosen(player1, pegasus.getId());
        harness.passBothPriorities();

        assertThat(pegasus.getPowerModifier()).isEqualTo(1);
        assertThat(pegasus.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves after Pegasus leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScaledHerbalist());
        Permanent pegasus = harness.enterBattlefieldAndReturn(player1, new ArboreaPegasus());
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pegasus));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Arborea Pegasus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB has no effect when its target leaves before resolution")
    void abilityDoesNotAffectDepartedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScaledHerbalist());
        Permanent pegasus = harness.enterBattlefieldAndReturn(player1, new ArboreaPegasus());
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(pegasus.getPowerModifier()).isZero();
        assertThat(pegasus.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player2, "Scaled Herbalist");
        assertThat(gd.stack).isEmpty();
    }
}
