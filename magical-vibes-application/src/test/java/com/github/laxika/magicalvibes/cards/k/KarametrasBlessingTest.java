package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Indestructibility;
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

@CardUsed({KarametrasBlessing.class, GrizzlyBears.class, Indestructibility.class,
        AlseidOfLifesBounty.class, FountainOfYouth.class})
class KarametrasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2")
    void boostsRegularCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bear);

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An enchanted creature also gains hexproof and indestructible")
    void protectsEnchantedCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new Indestructibility());
        aura.setAttachedTo(bear.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        castResolve(bear);

        assertProtectedAndBoosted(bear);
    }

    @Test
    @DisplayName("An enchantment creature also gains hexproof and indestructible")
    void protectsEnchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlseidOfLifesBounty());

        castResolve(creature);

        assertProtectedAndBoosted(creature);
    }

    @Test
    @DisplayName("The boost and granted keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bear);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void assertProtectedAndBoosted(Permanent creature) {
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
