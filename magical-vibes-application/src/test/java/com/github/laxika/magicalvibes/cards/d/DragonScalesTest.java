package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonScales.class, AvenFarseer.class, ElvishAberration.class})
class DragonScalesTest extends BaseCardTest {

    @Test
    void resolvingAuraBoostsEnchantedCreatureAndGrantsVigilance() {
        Permanent creature = addCreatureReady(player1, new AvenFarseer());
        harness.setHand(player1, List.of(new DragonScales()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Scales");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void sixManaValueCreatureEnteringUnderAnyPlayersControlTriggersReturn() {
        DragonScales scales = new DragonScales();
        harness.setGraveyard(player1, List.of(scales));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ElvishAberration());

        resolveMayAbility(true);

        Permanent returnedScales = findPermanent(player1, "Dragon Scales");
        assertThat(returnedScales.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Scales");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonScales()));
        harness.enterBattlefieldAndReturn(player1, new AvenFarseer());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Scales");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonScales()));
        harness.enterBattlefieldAndReturn(player1, new ElvishAberration());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Scales");
        harness.assertNotOnBattlefield(player1, "Dragon Scales");
    }

    @Test
    void acceptedReturnEntersUnattachedWhenEnteringCreatureLeavesBeforeResolution() {
        harness.setGraveyard(player1, List.of(new DragonScales()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new ElvishAberration());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gameLogContains("Dragon Scales returns to the battlefield unattached.")).isTrue();
        harness.assertInGraveyard(player1, "Dragon Scales");
        harness.assertNotOnBattlefield(player1, "Dragon Scales");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
