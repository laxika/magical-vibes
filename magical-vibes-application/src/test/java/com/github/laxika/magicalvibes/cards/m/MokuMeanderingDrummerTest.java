package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MokuMeanderingDrummer.class, GrizzlyBears.class, Spellbook.class})
class MokuMeanderingDrummerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} boosts Moku and grants haste to all your creatures")
    void payingBoostsMokuAndGrantsHaste() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(moku.getPowerModifier()).isEqualTo(2);
        assertThat(moku.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.HASTE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the payment leaves creatures unchanged")
    void decliningLeavesCreaturesUnchanged() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(moku.getPowerModifier()).isZero();
        assertThat(moku.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost and haste expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(moku.getPowerModifier()).isZero();
        assertThat(moku.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creature spells do not trigger Moku")
    void creatureSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new MokuMeanderingDrummer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Colored mana can pay the generic trigger cost")
    void coloredManaPaysForTrigger() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(moku.getPowerModifier()).isEqualTo(2);
        assertThat(moku.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Each paid noncreature spell trigger adds another boost")
    void multiplePaidTriggersAccumulate() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(moku.getPowerModifier()).isEqualTo(4);
        assertThat(moku.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not gain haste")
    void laterCreaturesDoNotGainHaste() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        Permanent laterBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterBear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Moku")
    void opponentsSpellDoesNotTrigger() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(moku.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Accepting without enough mana does not grant either effect")
    void unpaidTriggerDoesNotGrantEffects() {
        Permanent moku = harness.addToBattlefieldAndReturn(player1, new MokuMeanderingDrummer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(moku.getPowerModifier()).isZero();
        assertThat(moku.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, moku, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }
}
