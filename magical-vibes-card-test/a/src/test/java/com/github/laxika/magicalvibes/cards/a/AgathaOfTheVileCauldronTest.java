package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.j.JoustingDummy;
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

@CardUsed({AgathaOfTheVileCauldron.class, GiantGrowth.class, JoustingDummy.class})
class AgathaOfTheVileCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a creature's activated ability by Agatha's power")
    void reducesCreatureAbilityByPower() {
        harness.addToBattlefield(player1, new AgathaOfTheVileCauldron());
        harness.addToBattlefield(player1, new JoustingDummy());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Uses Agatha's current power for the reduction")
    void usesCurrentPowerForReduction() {
        Permanent agatha = harness.addToBattlefieldAndReturn(player1, new AgathaOfTheVileCauldron());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, agatha.getId());

        harness.addToBattlefield(player1, new JoustingDummy());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Boosts other creatures with trample and haste until end of turn")
    void boostsOtherCreaturesUntilEndOfTurn() {
        Permanent agatha = harness.addToBattlefieldAndReturn(player1, new AgathaOfTheVileCauldron());
        Permanent ownDummy = harness.addToBattlefieldAndReturn(player1, new JoustingDummy());
        Permanent opponentDummy = harness.addToBattlefieldAndReturn(player2, new JoustingDummy());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(agatha.getPowerModifier()).isZero();
        assertThat(ownDummy.getPowerModifier()).isEqualTo(1);
        assertThat(ownDummy.getToughnessModifier()).isEqualTo(1);
        assertThat(ownDummy.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(ownDummy.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentDummy.getPowerModifier()).isZero();
        assertThat(opponentDummy.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(opponentDummy.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownDummy.getPowerModifier()).isZero();
        assertThat(ownDummy.getToughnessModifier()).isZero();
        assertThat(ownDummy.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(ownDummy.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not reduce an opponent's creature ability")
    void doesNotReduceOpponentCreatureAbility() {
        harness.addToBattlefield(player1, new AgathaOfTheVileCauldron());
        harness.addToBattlefield(player2, new JoustingDummy());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void cannotMakeGenericManaAbilityFree() {
        Permanent agatha = harness.addToBattlefieldAndReturn(player1, new AgathaOfTheVileCauldron());
        harness.addToBattlefield(player1, new JoustingDummy());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, agatha.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).getPowerModifier()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reducesOwnAbilityToItsColoredManaCost() {
        Permanent agatha = harness.addToBattlefieldAndReturn(player1, new AgathaOfTheVileCauldron());
        Permanent dummy = harness.addToBattlefieldAndReturn(player1, new JoustingDummy());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, agatha.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(agatha.getPowerModifier()).isEqualTo(3);
        assertThat(agatha.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(agatha.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(dummy.getPowerModifier()).isEqualTo(1);
        assertThat(dummy.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(dummy.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        harness.addToBattlefield(player1, new AgathaOfTheVileCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new JoustingDummy());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new JoustingDummy());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(beforeResolution.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(beforeResolution.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
        assertThat(afterResolution.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(afterResolution.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
