package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({SpellbookVendor.class, GrizzlyBears.class, Forest.class})
class SpellbookVendorTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses the Role target after paying for the beginning-of-combat ability")
    void choosesRoleTargetAfterPayment() {
        addCreatureReady(player1, new SpellbookVendor());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the payment does not create a Role")
    void decliningPaymentDoesNotCreateRole() {
        addCreatureReady(player1, new SpellbookVendor());
        addCreatureReady(player1, new GrizzlyBears());
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
    }

    @Test
    @DisplayName("The Sorcerer Role grants scry 1 when its creature attacks")
    void sorcererRoleScriesWhenEnchantedCreatureAttacks() {
        addCreatureReady(player1, new SpellbookVendor());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @CardUsed({SpellbookVendor.class})
    @DisplayName("Paying creates a separate trigger that can be responded to before the Role enters")
    void roleCreationUsesSeparateReflexiveTrigger() {
        Permanent vendor = addCreatureReady(player1, new SpellbookVendor());
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, vendor.getId());

            assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
            assertThat(gd.stack).hasSize(1);

            harness.inMutationScope(() ->
                    harness.getPermanentRemovalService().removePermanentToHand(gd, vendor));
            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
        });
    }

    @Test
    @CardUsed({SpellbookVendor.class})
    @DisplayName("Spellbook Vendor can enchant itself")
    void canCreateRoleAttachedToItself() {
        Permanent vendor = addCreatureReady(player1, new SpellbookVendor());
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vendor.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sorcerer").getAttachedTo()).isEqualTo(vendor.getId());
        assertThat(gqs.getEffectivePower(gd, vendor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vendor)).isEqualTo(3);
    }

    @Test
    @CardUsed({SpellbookVendor.class})
    @DisplayName("A new Sorcerer Role replaces the controller's older Role on the same creature")
    void newRoleReplacesOlderRole() {
        Permanent vendor = addCreatureReady(player1, new SpellbookVendor());
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vendor.getId());
        resolveAllTriggers();
        Permanent oldRole = findPermanent(player1, "Sorcerer");

        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vendor.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sorcerer")).hasSize(1);
        assertThat(findPermanent(player1, "Sorcerer").getId()).isNotEqualTo(oldRole.getId());
        assertThat(gqs.getEffectivePower(gd, vendor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vendor)).isEqualTo(3);
    }

    @Test
    @CardUsed({SpellbookVendor.class})
    @DisplayName("Spellbook Vendor does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        addCreatureReady(player1, new SpellbookVendor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
