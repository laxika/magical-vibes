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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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
        harness.setHand(player, List.of(new GloriousAnthem()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player, 0);
    }
}
