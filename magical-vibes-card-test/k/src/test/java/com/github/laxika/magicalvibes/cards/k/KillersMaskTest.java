package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillersMask.class, GrizzlyBears.class, Forest.class})
class KillersMaskTest extends BaseCardTest {

    @Test
    void manifestsAndAttachesToTheManifestedCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new KillersMask(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent equipment = findPermanent(player1, "Killer's Mask");
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(equipment.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.MENACE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void equipGrantsMenaceToTheNewCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new KillersMask());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void manifestsTheOnlyCardAndAttachesEvenWhenItIsALand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.castFromHand(player1, new KillersMask(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(land);
        assertThat(findPermanent(player1, "Killer's Mask").getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.MENACE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryLeavesEquipmentUnattached() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new KillersMask(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Killer's Mask").getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void turningManifestedCreatureFaceUpKeepsEquipmentAndMenace() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.castFromHand(player1, new KillersMask(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(findPermanent(player1, "Killer's Mask").getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.MENACE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void movingEquipmentRemovesMenaceFromPreviousCreature() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.castFromHand(player1, new KillersMask(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        Permanent previous = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        Permanent next = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, next.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Killer's Mask").getAttachedTo()).isEqualTo(next.getId());
        assertThat(gqs.hasKeyword(gd, previous, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, next, Keyword.MENACE)).isTrue();
    }
}
