package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({JadeStatue.class, GrizzlyBears.class, EnsoulArtifact.class, RayOfCommand.class})
class JadeStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate the ability outside of combat")
    void cannotActivateOutsideCombat() {
        Permanent statue = addCreatureReady(player1, new JadeStatue());
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
    @DisplayName("Can activate the ability during an opponent's combat")
    void canActivateDuringOpponentsCombat() {
        Permanent statue = addCreatureReady(player1, new JadeStatue());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(6);
    }

    @Test
    @DisplayName("Activating during combat makes it a 3/6 Golem artifact creature")
    void animatesDuringCombat() {
        Permanent statue = addCreatureReady(player1, new JadeStatue());
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
        Permanent statue = addCreatureReady(player1, new JadeStatue());
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
        Permanent statue = addCreatureReady(player1, new JadeStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Animate during the beginning-of-combat step.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();
        assertThat(statue.isAnimatedUntilEndOfCombat()).isTrue();
        assertThat(gqs.isCreature(gd, statue)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(statue.isAnimatedUntilEndOfCombat()).isFalse();
        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(0);
        assertThat(statue.getTransientSubtypes()).doesNotContain(CardSubtype.GOLEM);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

    @Test
    @CardUsed({JadeStatue.class, RayOfCommand.class})
    @DisplayName("Changing control during combat does not end the animation")
    void remainsAnimatedAfterControlChanges() {
        Permanent statue = addCreatureReady(player1, new JadeStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, statue.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(statue);
        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(6);

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, statue)).isFalse();
    }

    @Test
    @DisplayName("Can animate in the end-of-combat step, then reverts as combat ends")
    void animatesDuringEndOfCombat() {
        Permanent statue = harness.addToBattlefieldAndReturn(player1, new JadeStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(6);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, statue)).isFalse();
    }

    @Test
    @DisplayName("Cannot animate without paying two mana")
    void cannotAnimateWithInsufficientMana() {
        Permanent statue = addCreatureReady(player1, new JadeStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(statue), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @CardUsed({JadeStatue.class, EnsoulArtifact.class})
    @DisplayName("Animation overrides an earlier base power and toughness effect only until combat ends")
    void animationOverridesEarlierEnsoulArtifact() {
        Permanent statue = addCreatureReady(player1, new JadeStatue());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, statue.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(5);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(statue), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(6);

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(5);
    }
}
