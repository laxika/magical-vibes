package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.g.GriffinGuide;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({CloudchaserKestrel.class, GriffinGuide.class, AshcoatBear.class, CandlesOfLeng.class})
class CloudchaserKestrelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys target enchantment")
    void entersAndDestroysTargetEnchantment() {
        Permanent target = addAttachedGriffinGuide();
        harness.setHand(player1, List.of(new CloudchaserKestrel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cloudchaser Kestrel");
        harness.assertNotOnBattlefield(player2, "Griffin Guide");
        harness.assertInGraveyard(player2, "Griffin Guide");
    }

    @Test
    @DisplayName("Cannot target a creature with the enter-the-battlefield ability")
    void cannotTargetCreatureWithEnterTheBattlefieldAbility() {
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new CloudchaserKestrel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, 0, harness.getPermanentId(player2, "Ashcoat Bear")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Can cast without an enchantment target when none is available")
    void canCastWithoutEnchantmentTargetWhenNoneIsAvailable() {
        harness.castFromHand(player1, new CloudchaserKestrel(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudchaser Kestrel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses an enchantment when the enter-the-battlefield trigger is put on the stack")
    void choosesEnchantmentWhenEnterTheBattlefieldTriggerIsPutOnStack() {
        harness.castFromHand(player1, new CloudchaserKestrel(), "{1}{W}{W}");
        Permanent target = addAttachedGriffinGuide();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Griffin Guide");
        harness.assertInGraveyard(player2, "Griffin Guide");
    }

    @Test
    @DisplayName("Target permanent becomes white until end of turn")
    void targetPermanentBecomesWhiteUntilEndOfTurn() {
        harness.addToBattlefield(player1, new CloudchaserKestrel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Can target a noncreature permanent with the color ability")
    void canTargetNoncreaturePermanentWithColorAbility() {
        harness.addToBattlefield(player1, new CloudchaserKestrel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Must destroy its controller's enchantment when it is the only enchantment")
    void destroysOwnEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GriffinGuide());
        aura.setAttachedTo(creature.getId());

        harness.castFromHand(player1, new CloudchaserKestrel(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Griffin Guide");
        harness.assertNotOnBattlefield(player1, "Griffin Guide");
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent kestrel = harness.addToBattlefieldAndReturn(player1, new CloudchaserKestrel());
        kestrel.tap();
        kestrel.setSummoningSick(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveColors(gd, artifact)).containsExactly(CardColor.WHITE);
        assertThat(kestrel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Color ability resolves after its source leaves the battlefield")
    void colorAbilityResolvesAfterSourceLeavesBattlefield() {
        Permanent kestrel = harness.addToBattlefieldAndReturn(player1, new CloudchaserKestrel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kestrel));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cloudchaser Kestrel");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    private Permanent addAttachedGriffinGuide() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GriffinGuide());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
