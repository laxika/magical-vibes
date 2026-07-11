package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MutavaultTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mutavault produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent mutavault = addMutavaultReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mutavault);

        gs.tapPermanent(gd, player1, index);

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
        // "all creature types" is modelled as Changeling.
        assertThat(gqs.hasKeyword(gd, mutavault, Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Mutavault is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent mutavault = addMutavaultReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mutavault.getCard().getType()).isEqualTo(CardType.LAND);
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
        assertThat(mutavault.getCard().getType()).isEqualTo(CardType.LAND);
    }

    private Permanent addMutavaultReady(Player player) {
        Mutavault card = new Mutavault();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
