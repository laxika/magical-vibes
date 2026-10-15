package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuperMutantScavenger.class, Oakenform.class, Bonesplitter.class, GrizzlyBears.class})
class SuperMutantScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted Aura card from the graveyard to hand")
    void returnsAuraOnEntering() {
        Oakenform oakenform = new Oakenform();
        harness.setGraveyard(player1, List.of(oakenform));

        castScavenger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(oakenform.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oakenform");
        harness.assertNotInGraveyard(player1, "Oakenform");
    }

    @Test
    @DisplayName("Death trigger returns a targeted Equipment card from the graveyard to hand")
    void returnsEquipmentOnDeath() {
        Bonesplitter bonesplitter = new Bonesplitter();
        harness.setGraveyard(player1, List.of(bonesplitter));
        Permanent scavenger = addCreatureReady(player1, new SuperMutantScavenger());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, scavenger));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bonesplitter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bonesplitter");
        harness.assertNotInGraveyard(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("Triggers cannot target a non-Aura, non-Equipment card")
    void doesNotTargetOtherCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castScavenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger may decline to return a card")
    void mayDeclineToReturnCard() {
        Oakenform oakenform = new Oakenform();
        harness.setGraveyard(player1, List.of(oakenform));

        castScavenger();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oakenform");
        harness.assertNotInHand(player1, "Oakenform");
    }

    @Test
    @DisplayName("ETB returns Equipment as well as Auras")
    void returnsEquipmentOnEntering() {
        Bonesplitter equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(equipment));

        castScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bonesplitter");
        harness.assertNotInGraveyard(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("Death returns Auras as well as Equipment")
    void returnsAuraOnDeath() {
        Oakenform aura = new Oakenform();
        harness.setGraveyard(player1, List.of(aura));
        Permanent scavenger = addCreatureReady(player1, new SuperMutantScavenger());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, scavenger));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oakenform");
        harness.assertNotInGraveyard(player1, "Oakenform");
    }

    @Test
    @DisplayName("The trigger returns only one card when an Aura and Equipment are available")
    void returnsOnlyOneMatchingCard() {
        Oakenform aura = new Oakenform();
        Bonesplitter equipment = new Bonesplitter();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(aura, equipment, creature));

        castScavenger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactlyInAnyOrder(aura, equipment);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Oakenform");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Oakenform");
    }

    @Test
    @DisplayName("The trigger cannot target cards in an opponent's graveyard")
    void onlyTargetsControllersGraveyard() {
        Oakenform ownAura = new Oakenform();
        Bonesplitter opposingEquipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(ownAura));
        harness.setGraveyard(player2, List.of(opposingEquipment));

        castScavenger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(ownAura);
        harness.handleMultipleCardsChosen(player1, List.of(ownAura.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oakenform");
        harness.assertInGraveyard(player2, "Bonesplitter");
        harness.assertNotInHand(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not returned and cannot be replaced")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        Oakenform aura = new Oakenform();
        Bonesplitter equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(aura, equipment));

        castScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.setGraveyard(player1, List.of(equipment));
        harness.setExile(player1, List.of(aura));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Oakenform");
        harness.assertNotInHand(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Bonesplitter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The death trigger may choose zero targets")
    void mayDeclineDeathReturn() {
        harness.setGraveyard(player1, List.of(new Bonesplitter()));
        Permanent scavenger = addCreatureReady(player1, new SuperMutantScavenger());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, scavenger));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertNotInHand(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Super Mutant Scavenger");
    }

    private void castScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SuperMutantScavenger(), "{4}{G}");
        harness.passBothPriorities();
    }
}
