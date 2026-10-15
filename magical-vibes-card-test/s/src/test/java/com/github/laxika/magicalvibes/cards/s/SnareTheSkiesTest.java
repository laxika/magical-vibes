package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({SnareTheSkies.class, GrizzlyBears.class, Pacifism.class, SeraphOfDawn.class})
class SnareTheSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Snare the Skies gives +1/+1 and reach")
    void boostsAndGrantsReach() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnareTheSkies()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Boost and reach expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnareTheSkies()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new SnareTheSkies()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Repeated casts boost only the targeted creature you control")
    void repeatedCastsStackOnlyOnTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnareTheSkies(), new SnareTheSkies()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Granted reach permits blocking a flying creature only until end of turn")
    void reachAllowsBlockingFlyingUntilEndOfTurn() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SeraphOfDawn());
        harness.setHand(player1, List.of(new SnareTheSkies()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, flyer, gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, flyer, gd.playerBattlefields.get(player1.getId()))).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, flyer, gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("A removed target receives neither boost nor reach")
    void removedTargetDoesNotReceiveEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SnareTheSkies spell = new SnareTheSkies();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }
}
