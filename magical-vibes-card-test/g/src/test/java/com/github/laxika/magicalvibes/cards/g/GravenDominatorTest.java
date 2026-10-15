package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravenDominator.class, DryadSophisticate.class, Mortify.class, GodlessShrine.class})
class GravenDominatorTest extends BaseCardTest {

    @Test
    void enteringSetsEachOtherCreatureToOneOneUntilEndOfTurn() {
        harness.addToBattlefield(player1, new DryadSophisticate());
        harness.addToBattlefield(player2, new DryadSophisticate());
        Permanent ownCreature = findPermanent(player1, "Dryad Sophisticate");
        Permanent opposingCreature = findPermanent(player2, "Dryad Sophisticate");

        castGravenDominator();

        Permanent gravenDominator = findPermanent(player1, "Graven Dominator");
        assertThat(gqs.getEffectivePower(gd, gravenDominator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gravenDominator)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    void enteringDoesNotAffectNoncreaturePermanents() {
        harness.addToBattlefield(player2, new DryadSophisticate());
        harness.addToBattlefield(player2, new GodlessShrine());

        castGravenDominator();

        Permanent creature = findPermanent(player2, "Dryad Sophisticate");
        Permanent shrine = findPermanent(player2, "Godless Shrine");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, shrine)).isFalse();
    }

    @Test
    void hauntOnlyOffersCreatureTargets() {
        harness.addToBattlefield(player2, new GodlessShrine());
        harness.addToBattlefield(player2, new DryadSophisticate());
        castGravenDominator();

        UUID gravenDominatorId = harness.getPermanentId(player1, "Graven Dominator");
        UUID shrineId = harness.getPermanentId(player2, "Godless Shrine");
        UUID creatureId = harness.getPermanentId(player2, "Dryad Sophisticate");
        destroyWithMortify(gravenDominatorId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creatureId)
                .doesNotContain(shrineId);
    }

    @Test
    void doesNotHauntWhenNoCreatureIsAvailable() {
        castGravenDominator();

        UUID gravenDominatorId = harness.getPermanentId(player1, "Graven Dominator");
        destroyWithMortify(gravenDominatorId);

        harness.assertInGraveyard(player1, "Graven Dominator");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Graven Dominator");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void hauntedCreatureDeathSetsOtherCreaturesToOneOne() {
        harness.addToBattlefield(player2, new DryadSophisticate());
        harness.addToBattlefield(player2, new DryadSophisticate());
        Permanent hauntedCreature = findPermanents(player2, "Dryad Sophisticate").getFirst();
        Permanent survivingCreature = findPermanents(player2, "Dryad Sophisticate").get(1);

        castGravenDominator();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        UUID gravenDominatorId = harness.getPermanentId(player1, "Graven Dominator");
        destroyWithMortify(gravenDominatorId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Graven Dominator");

        destroyWithMortify(hauntedCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivingCreature)).isEqualTo(1);
    }

    @Test
    void creaturesEnteringAfterResolutionKeepTheirBaseStats() {
        castGravenDominator();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player2, new DryadSophisticate());

        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(1);
    }

    @Test
    void countersApplyOnTopOfTheOneOneBaseStats() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DryadSophisticate());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castGravenDominator();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
    private void castGravenDominator() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GravenDominator(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithMortify(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
