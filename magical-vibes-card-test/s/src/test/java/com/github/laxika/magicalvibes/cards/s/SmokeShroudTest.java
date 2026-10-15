package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.m.MoonbladeShinobi;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmokeShroud.class, SnowCoveredForest.class, MotherBear.class, MoonbladeShinobi.class})
class SmokeShroudTest extends BaseCardTest {

    @Test
    void resolvingAuraBoostsEnchantedCreatureAndGrantsFlying() {
        Permanent bears = addCreatureReady(player1, new MotherBear());
        harness.setHand(player1, List.of(new SmokeShroud()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Smoke Shroud");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void ninjaEnteringUnderYourControlReturnsAuraAttachedToIt() {
        SmokeShroud shroud = new SmokeShroud();
        harness.setGraveyard(player1, List.of(shroud));

        Permanent ninja = harness.enterBattlefieldAndReturn(player1, new MoonbladeShinobi());

        resolveMayAbility(true);

        Permanent returnedShroud = findPermanent(player1, "Smoke Shroud");
        assertThat(returnedShroud.getAttachedTo()).isEqualTo(ninja.getId());
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Smoke Shroud");
    }

    @Test
    void nonNinjaOrOpponentCreatureEnteringDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new SmokeShroud()));

        harness.enterBattlefieldAndReturn(player1, new MotherBear());
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player2, new MoonbladeShinobi());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Smoke Shroud");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new SmokeShroud()));
        harness.enterBattlefieldAndReturn(player1, new MoonbladeShinobi());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Smoke Shroud");
        harness.assertNotOnBattlefield(player1, "Smoke Shroud");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        Permanent forest = findPermanent(player1, "Snow-Covered Forest");
        harness.setHand(player1, List.of(new SmokeShroud()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void auraStaysInGraveyardIfEnteringNinjaLeavesBeforeResolution() {
        harness.setGraveyard(player1, List.of(new SmokeShroud()));
        Permanent ninja = harness.enterBattlefieldAndReturn(player1, new MoonbladeShinobi());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ninja);

        resolveMayAbility(true);

        harness.assertInGraveyard(player1, "Smoke Shroud");
        harness.assertNotOnBattlefield(player1, "Smoke Shroud");
        assertThat(gameLogContains("returns to the battlefield unattached")).isFalse();
    }

    @Test
    void auraCannotReturnIfItLeftTheGraveyardBeforeResolution() {
        harness.setGraveyard(player1, List.of(new SmokeShroud()));
        harness.enterBattlefieldAndReturn(player1, new MoonbladeShinobi());
        harness.setGraveyard(player1, List.of());

        resolveMayAbility(true);

        harness.assertNotOnBattlefield(player1, "Smoke Shroud");
        harness.assertNotInGraveyard(player1, "Smoke Shroud");
    }

    @Test
    void auraCanEnchantAnOpponentsCreature() {
        Permanent bear = addCreatureReady(player2, new MotherBear());
        harness.setHand(player1, List.of(new SmokeShroud()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Smoke Shroud").getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
