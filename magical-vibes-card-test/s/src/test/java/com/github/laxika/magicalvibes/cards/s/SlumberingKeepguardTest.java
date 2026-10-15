package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlumberingKeepguard.class, GloriousAnthem.class, AuraOfSilence.class, GrizzlyBears.class})
class SlumberingKeepguardTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control makes you scry 1")
    void allyEnchantmentEntryTriggersScry() {
        harness.addToBattlefield(player1, new SlumberingKeepguard());
        castGloriousAnthem(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-enchantment entering under your control does not trigger")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SlumberingKeepguard());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SlumberingKeepguard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castGloriousAnthem(player2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activated ability boosts this creature by the number of enchantments you control")
    void activatedAbilityBoostsPerEnchantmentUntilEndOfTurn() {
        Permanent keepguard = addCreatureReady(player1, new SlumberingKeepguard());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, keepguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keepguard)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, keepguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, keepguard)).isEqualTo(1);
    }

    private void castGloriousAnthem(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new GloriousAnthem(), "{1}{W}{W}");
    }

    @Test
    @DisplayName("Opponent enchantments do not contribute to the activated ability")
    void noControlledEnchantmentsMeansNoBoost() {
        Permanent keepguard = harness.addToBattlefieldAndReturn(player1, new SlumberingKeepguard());
        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, keepguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, keepguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchantments are counted at resolution, not activation")
    void enchantmentSacrificedInResponseDoesNotContribute() {
        Permanent keepguard = harness.addToBattlefieldAndReturn(player1, new SlumberingKeepguard());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfSilence());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.sacrificePermanent(player1, 1, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gqs.getEffectivePower(gd, keepguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, keepguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations work while tapped and summoning sick and keep their resolved bonus")
    void repeatedActivationsStackAndDoNotRecountAfterResolution() {
        Permanent keepguard = harness.addToBattlefieldAndReturn(player1, new SlumberingKeepguard());
        keepguard.tap();
        keepguard.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfSilence());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, keepguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keepguard)).isEqualTo(3);

        harness.sacrificePermanent(player1, 1, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gqs.getEffectivePower(gd, keepguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keepguard)).isEqualTo(3);
    }
}
