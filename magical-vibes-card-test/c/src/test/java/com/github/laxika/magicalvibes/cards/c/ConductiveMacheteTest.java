package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConductiveMachete.class, GrizzlyBears.class, Forest.class})
class ConductiveMacheteTest extends BaseCardTest {

    @Test
    void manifestsAndAttachesToTheManifestedCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new ConductiveMachete()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent equipment = findPermanent(player1, "Conductive Machete");
        Permanent manifested = findPermanent(player1, "Grizzly Bears");
        assertThat(equipment.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void equipGrantsPlusTwoPlusOneToTheNewCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ConductiveMachete());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void manifestsTheOnlyCardEvenWhenItIsALand() {
        Card land = new Forest();
        harness.setHand(player1, List.of(new ConductiveMachete()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        Permanent manifested = findPermanent(player1, "Forest");
        assertThat(manifested.isManifested()).isTrue();
        assertThat(findPermanent(player1, "Conductive Machete").getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canManifestTheSecondCardAndOnlyLooksAtTheTopTwo() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setHand(player1, List.of(new ConductiveMachete()));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards())
                .containsExactly(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        Permanent manifested = findPermanent(player1, "Forest");
        assertThat(manifested.getCard()).isSameAs(second);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(findPermanent(player1, "Conductive Machete").getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void emptyLibraryLeavesEquipmentUnattachedWithoutChoosingAnExistingCreature() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConductiveMachete()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Conductive Machete").getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
    }

    @Test
    void remainsAttachedWhenManifestTurnsFaceUpAndTransfersBonusWhenReequipped() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new ConductiveMachete()));
        harness.setLibrary(player1, List.of(bears, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent equipment = findPermanent(player1, "Conductive Machete");
        Permanent manifested = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(equipment.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);

        Permanent newHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(equipment),
                null, newHost.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newHost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, newHost)).isEqualTo(3);
    }
}
