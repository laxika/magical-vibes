package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanCloudscraper;
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

@CardUsed({DragonBreath.class, FountainOfYouth.class, GrizzlyBears.class, KrosanCloudscraper.class})
class DragonBreathTest extends BaseCardTest {

    @Test
    void resolvingAuraGrantsHasteToEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonBreath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Breath");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    void activatedAbilityBoostsEnchantedCreatureUntilEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new DragonBreath());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void highManaValueCreatureEnteringTriggersReturnAttachedToIt() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new KrosanCloudscraper());

        resolveMayAbility(true);

        Permanent returnedBreath = findPermanent(player1, "Dragon Breath");
        assertThat(returnedBreath.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Breath");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Breath");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonBreath()));
        harness.enterBattlefieldAndReturn(player1, new KrosanCloudscraper());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Breath");
        harness.assertNotOnBattlefield(player1, "Dragon Breath");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DragonBreath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

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
