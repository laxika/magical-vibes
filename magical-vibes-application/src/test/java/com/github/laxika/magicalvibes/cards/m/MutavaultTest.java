package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Mutavault.class, SuddenSpoiling.class})
class MutavaultTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mutavault produces colorless mana")
    void tappingProducesColorlessMana() {
        addMutavaultReady(player1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving the ability makes it a 2/2 creature with all creature types")
    void resolvingAbilityMakesItAnimated() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mutavault.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, mutavault)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mutavault)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mutavault)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, mutavault, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mutavault, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, mutavault, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Mutavault is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, mutavault)).isTrue();
        assertThat(gqs.isCreature(gd, mutavault)).isTrue();
    }

    @Test
    @DisplayName("Activating the ability does NOT tap the permanent and consumes the mana")
    void activatingDoesNotTapAndConsumesMana() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mutavault.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mutavault)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mutavault.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, mutavault)).isFalse();
        assertThat(gqs.hasKeyword(gd, mutavault, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Mutavault is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent mutavault = addMutavaultReady(player1);

        assertThat(gqs.isCreature(gd, mutavault)).isFalse();
        assertThat(gqs.isLand(gd, mutavault)).isTrue();
    }

    @Test
    @DisplayName("Losing abilities does not remove the creature types granted by animation")
    void keepsCreatureTypesAfterLosingAbilities() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SuddenSpoiling()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.isCreature(gd, mutavault)).isTrue();
        assertThat(gqs.isLand(gd, mutavault)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mutavault)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, mutavault)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, mutavault, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mutavault, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, mutavault, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Mutavault can pay for its own animation while tapped")
    void paysForOwnAnimation() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.tapPermanent(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.isCreature(gd, mutavault)).isFalse();
        harness.passBothPriorities();

        assertThat(mutavault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.isCreature(gd, mutavault)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mutavault)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mutavault)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated animation remains 2/2 and does not animate other lands")
    void repeatedAnimationOnlyAffectsSource() {
        Permanent mutavault = addMutavaultReady(player1);
        Permanent other = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mutavault)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mutavault)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, other)).isFalse();
    }

    private Permanent addMutavaultReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Mutavault());
        perm.setSummoningSick(false);
        return perm;
    }
}
