package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JadeStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate the ability outside of combat")
    void cannotActivateOutsideCombat() {
        Permanent statue = addStatueReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int index = indexOf(statue);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, statue)).isFalse();
    }

    @Test
    @DisplayName("Activating during combat makes it a 3/6 Golem artifact creature")
    void animatesDuringCombat() {
        Permanent statue = addStatueReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(statue.isAnimatedUntilEndOfCombat()).isTrue();
        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.isArtifact(statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(6);
        assertThat(statue.getTransientSubtypes()).contains(CardSubtype.GOLEM);
    }

    @Test
    @DisplayName("Mana is consumed when activating the ability")
    void manaIsConsumed() {
        Permanent statue = addStatueReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(statue), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Animation reverts when combat ends")
    void revertsAtEndOfCombat() {
        Permanent statue = addStatueReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Animate during the beginning-of-combat step.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();
        assertThat(statue.isAnimatedUntilEndOfCombat()).isTrue();
        assertThat(gqs.isCreature(gd, statue)).isTrue();

        // Declare no attackers, then let priority passes cascade combat to its end.
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of());
        for (int i = 0; i < 8 && statue.isAnimatedUntilEndOfCombat(); i++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }

        assertThat(statue.isAnimatedUntilEndOfCombat()).isFalse();
        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(0);
        assertThat(statue.getTransientSubtypes()).doesNotContain(CardSubtype.GOLEM);
    }

    private Permanent addStatueReady() {
        Permanent perm = new Permanent(new JadeStatue());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
