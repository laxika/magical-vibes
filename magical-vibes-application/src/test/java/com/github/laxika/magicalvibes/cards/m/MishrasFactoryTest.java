package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MishrasFactoryTest extends BaseCardTest {

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Mishra's Factory produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent factory = addFactoryReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(factory);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("{1} makes it a 2/2 Assembly-Worker artifact creature that's still a land")
    void animateMakesItACreature() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, factory)).isTrue();
        assertThat(gqs.isArtifact(factory)).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(2);
        assertThat(factory.getTransientSubtypes()).contains(CardSubtype.ASSEMBLY_WORKER);
        assertThat(factory.getCard().getType()).isEqualTo(CardType.LAND);
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, factory)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(factory.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, factory)).isFalse();
        assertThat(gqs.isArtifact(factory)).isFalse();
    }

    // ===== Pump ability =====

    @Test
    @DisplayName("{T} pump gives an animated Mishra's Factory +1/+1")
    void pumpBoostsAssemblyWorker() {
        Permanent factory = addFactoryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Animate it so it is a legal Assembly-Worker creature target.
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // {T}: it targets itself as an Assembly-Worker creature.
        harness.activateAbility(player1, 0, 1, null, factory.getId());
        harness.passBothPriorities();

        assertThat(factory.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(3);
    }

    @Test
    @DisplayName("{T} pump cannot target a non-Assembly-Worker creature")
    void pumpCannotTargetNonAssemblyWorker() {
        addFactoryReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()));
    }

    // ===== Helpers =====

    private Permanent addFactoryReady(Player player) {
        Permanent perm = new Permanent(new MishrasFactory());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
