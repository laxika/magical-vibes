package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedReturnAuraAttachedToPermanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NextOfKin.class, DoomBlade.class, HillGiant.class, GrizzlyBears.class})
class NextOfKinTest extends BaseCardTest {

    @Test
    @DisplayName("puts a lower-mana-value creature from hand and reattaches at the next end step")
    void putsCreatureFromHandAndReattachesAura() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        NextOfKin aura = new NextOfKin();
        Card replacement = new GrizzlyBears();
        castAura(dyingCreature, aura);

        destroyDyingCreature(dyingCreature.getId(), replacement);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        assertThat(choice.includeGraveyard()).isFalse();
        assertThat(choice.includeCommandZone()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedReturnAuraAttachedToPermanent.class))
                .contains(new DelayedReturnAuraAttachedToPermanent(
                        aura.getId(), player1.getId(), entered.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);

        returnAuraAtNextEndStep();
        Permanent returnedAura = findPermanent(player1, "Next of Kin");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(entered.getId());
    }

    @Test
    @DisplayName("can put a lower-mana-value creature from the command zone")
    void putsCreatureFromCommandZoneAndReattachesAura() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        NextOfKin aura = new NextOfKin();
        Card replacement = new GrizzlyBears();
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(replacement)));
        castAura(dyingCreature, aura);

        destroyDyingCreature(dyingCreature.getId(), null);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(replacement);

        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(entered.getEnteredFromZone()).isEqualTo(com.github.laxika.magicalvibes.model.Zone.COMMAND);

        returnAuraAtNextEndStep();
        assertThat(findPermanent(player1, "Next of Kin").getAttachedTo()).isEqualTo(entered.getId());
    }

    @Test
    void mayDeclinePuttingCreatureOntoBattlefield() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        NextOfKin aura = new NextOfKin();
        Card replacement = new GrizzlyBears();
        castAura(dyingCreature, aura);
        destroyDyingCreature(dyingCreature.getId(), replacement);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(replacement);
        returnAuraAtNextEndStep();
        harness.assertNotOnBattlefield(player1, "Next of Kin");
        harness.assertInGraveyard(player1, "Next of Kin");
    }

    @Test
    void excludesEqualManaValueNoncreaturesAndGraveyardCards() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        castAura(dyingCreature, new NextOfKin());
        Card replacement = new GrizzlyBears();
        Card equalManaValue = new HillGiant();
        Card noncreature = new NextOfKin();
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new DoomBlade(), replacement, equalManaValue, noncreature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, dyingCreature.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentCreatureDeathUsesAuraControllersHand() {
        Permanent dyingCreature = addCreatureReady(player2, new HillGiant());
        castAura(dyingCreature, new NextOfKin());
        Card replacement = new GrizzlyBears();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        destroyDyingCreature(dyingCreature.getId(), replacement);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        returnAuraAtNextEndStep();
        assertThat(findPermanent(player1, "Next of Kin").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Grizzly Bears").getId());
    }

    @Test
    void doesNotReturnAuraIfReplacementCreatureDiesBeforeEndStep() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        castAura(dyingCreature, new NextOfKin());
        Card replacement = new GrizzlyBears();
        destroyDyingCreature(dyingCreature.getId(), replacement);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        destroyDyingCreature(findPermanent(player1, "Grizzly Bears").getId(), null);

        returnAuraAtNextEndStep();

        harness.assertNotOnBattlefield(player1, "Next of Kin");
        harness.assertInGraveyard(player1, "Next of Kin");
    }

    @Test
    void delayedReturnUsesStackAndAllowsResponses() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        castAura(dyingCreature, new NextOfKin());
        Card replacement = new GrizzlyBears();
        destroyDyingCreature(dyingCreature.getId(), replacement);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Next of Kin");
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Next of Kin");
    }

    @Test
    void returnsAuraUnderTriggerControllersControlRatherThanOwners() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        NextOfKin aura = new NextOfKin();
        aura.setOwnerId(player2.getId());
        castAura(dyingCreature, aura);
        Card replacement = new GrizzlyBears();
        destroyDyingCreature(dyingCreature.getId(), replacement);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));

        returnAuraAtNextEndStep();

        harness.assertNotOnBattlefield(player2, "Next of Kin");
        assertThat(findPermanent(player1, "Next of Kin").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Grizzly Bears").getId());
    }

    private void castAura(Permanent creature, NextOfKin aura) {
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void destroyDyingCreature(java.util.UUID creatureId, Card replacement) {
        List<Card> hand = new ArrayList<>(List.of(new DoomBlade()));
        if (replacement != null) {
            hand.add(replacement);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creatureId);
        resolveAllTriggers();
    }

    private void returnAuraAtNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
