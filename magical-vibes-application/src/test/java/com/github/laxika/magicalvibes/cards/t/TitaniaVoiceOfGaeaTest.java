package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArgothSanctumOfNature;
import com.github.laxika.magicalvibes.cards.a.ArgothianOpportunist;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TitaniaVoiceOfGaea.class, TitaniaGaeaIncarnate.class, ArgothSanctumOfNature.class,
        Forest.class, ArgothianOpportunist.class})
class TitaniaVoiceOfGaeaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when a land card is put into its controller's graveyard")
    void gainsLifeWhenOwnLandIsPutIntoGraveyard() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLife(player1, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Melds with Argoth at upkeep and returns graveyard lands tapped")
    void meldsWithArgothAtUpkeep() {
        Permanent titania = harness.addToBattlefieldAndReturn(player1, new TitaniaVoiceOfGaea());
        harness.addToBattlefield(player1, new Forest());
        Permanent argoth = harness.addToBattlefieldAndReturn(player1, new ArgothSanctumOfNature());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        assertThat(gqs.isLand(gd, argoth)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .allMatch(card -> card.hasType(CardType.LAND));

        advanceToUpkeep(player1);
        assertThat(gd.stack).anyMatch(entry -> entry.getSourcePermanentId().equals(titania.getId()));
        resolveAllTriggers();

        Permanent melded = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof TitaniaGaeaIncarnate)
                .findFirst().orElseThrow();
        assertThat(melded.getMeldComponentCards()).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(4);
        assertThat(gqs.getEffectivePower(gd, melded)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, melded)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animates a target land with four +1/+1 counters permanently")
    void animatesTargetLand() {
        harness.addToBattlefield(player1, new TitaniaGaeaIncarnate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    @Test
    void copiedArgothIsExiledButCannotMeld() {
        Permanent titania = harness.addToBattlefieldAndReturn(player1, new TitaniaVoiceOfGaea());
        Permanent copy = harness.addToBattlefieldAndReturn(player1, new Forest());
        copy.setCard(new ArgothSanctumOfNature());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TitaniaGaeaIncarnate);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(titania, copy);
        assertThat(gd.findExiledCard(titania.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(copy.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void upkeepConditionIsRecheckedWhenTriggerResolves() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        harness.addToBattlefield(player1, new ArgothSanctumOfNature());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Titania, Voice of Gaea");
        harness.assertOnBattlefield(player1, "Argoth, Sanctum of Nature");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TitaniaGaeaIncarnate);
    }

    @Test
    void meldedFaceReturnsOnlyItsControllersLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(new Forest(), new ArgothianOpportunist()));
        harness.setGraveyard(player2, List.of(new Forest()));

        Permanent titania = harness.enterBattlefieldAndReturn(player1, new TitaniaGaeaIncarnate());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof ArgothianOpportunist);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest).hasSize(2)
                .filteredOn(Permanent::isTapped).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, titania)).isEqualTo(2);
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, titania)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, titania)).isEqualTo(3);
    }

    @Test
    void meldedFaceCannotAnimateOpponentsLand() {
        harness.addToBattlefield(player1, new TitaniaGaeaIncarnate());
        harness.addToBattlefield(player1, new Forest());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainsLifeOnceForMultipleLandsMilledTogether() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        harness.addToBattlefield(player1, new ArgothSanctumOfNature());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new ArgothianOpportunist()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void opponentsLandDoesNotGainLife() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLife(player1, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void threeLandsDoNotTriggerMeld() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        harness.addToBattlefield(player1, new ArgothSanctumOfNature());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Titania, Voice of Gaea");
        harness.assertOnBattlefield(player1, "Argoth, Sanctum of Nature");
    }

    @Test
    void copiedTitaniaAndRealArgothRemainExiledWithoutMelding() {
        Permanent copy = harness.addToBattlefieldAndReturn(player1, new ArgothianOpportunist());
        copy.setCard(new TitaniaVoiceOfGaea());
        Permanent argoth = harness.addToBattlefieldAndReturn(player1, new ArgothSanctumOfNature());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy, argoth);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TitaniaGaeaIncarnate);
        assertThat(gd.findExiledCard(copy.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(argoth.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void controllerChoosesWhichArgothToExileWhenMultipleAreEligible() {
        harness.addToBattlefield(player1, new TitaniaVoiceOfGaea());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArgothSanctumOfNature());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArgothSanctumOfNature());
        second.tap();
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertOnBattlefield(player1, "Titania, Gaea Incarnate");
    }

    @Test
    void repeatedAnimationAccumulatesCountersAndSurvivesSourceLeavingAndTurnEnding() {
        Permanent titania = harness.addToBattlefieldAndReturn(player1, new TitaniaGaeaIncarnate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, titania));
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
    }

    @Test
    void repeatedAnimationAddsCountersAndSurvivesNextUpkeep() {
        harness.addToBattlefield(player1, new TitaniaGaeaIncarnate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, land.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, land.getId());
        resolveAllTriggers();
        advanceToUpkeep(player2);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
    }

    @Test
    void animationResolvesAfterTitaniaLeavesTheBattlefield() {
        Permanent titania = harness.addToBattlefieldAndReturn(player1, new TitaniaGaeaIncarnate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, titania));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    @Test
    void meldedFaceCannotAnimateANonlandCreature() {
        harness.addToBattlefield(player1, new TitaniaGaeaIncarnate());
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianOpportunist());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
