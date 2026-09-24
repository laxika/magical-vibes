package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.n.NobleTemplar;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonBreath.class, AvenFarseer.class, NobleTemplar.class, Stabilizer.class})
class DragonBreathTest extends BaseCardTest {

    @Test
    void resolvingAuraGrantsHasteToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new AvenFarseer());
        harness.setHand(player1, List.of(new DragonBreath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Breath");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void resolvingAuraCanEnchantCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player2, new AvenFarseer());
        harness.setHand(player1, List.of(new DragonBreath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Breath");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void activatedAbilityBoostsEnchantedCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new AvenFarseer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DragonBreath());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
    }

    @Test
    void sixManaValueCreatureEnteringTriggersReturnAttachedToIt() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new NobleTemplar());

        resolveMayAbility(true);

        Permanent returnedBreath = findPermanent(player1, "Dragon Breath");
        assertThat(returnedBreath.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Breath");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        harness.enterBattlefieldAndReturn(player1, new AvenFarseer());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Breath");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        harness.enterBattlefieldAndReturn(player1, new NobleTemplar());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Breath");
        harness.assertNotOnBattlefield(player1, "Dragon Breath");
    }

    @Test
    void acceptedReturnEntersUnattachedWhenEnteringCreatureLeavesBeforeResolution() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new NobleTemplar());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gameLogContains("Dragon Breath returns to the battlefield unattached.")).isTrue();
        harness.assertInGraveyard(player1, "Dragon Breath");
        harness.assertNotOnBattlefield(player1, "Dragon Breath");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Stabilizer());
        harness.setHand(player1, List.of(new DragonBreath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
