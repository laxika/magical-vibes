package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CursedWindbreaker.class, GrizzlyBears.class, Forest.class})
class CursedWindbreakerTest extends BaseCardTest {

    @Test
    void manifestsAndAttachesToTheManifestedCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new CursedWindbreaker()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent equipment = findPermanent(player1, "Cursed Windbreaker");
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(equipment.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void equipGrantsFlyingToTheNewCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new CursedWindbreaker());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void canManifestTheSecondCardEvenWhenItIsALand() {
        Card graveyardCard = new CursedWindbreaker();
        Card manifestedCard = new Forest();
        harness.setHand(player1, List.of(new CursedWindbreaker()));
        harness.setLibrary(player1, List.of(graveyardCard, manifestedCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(manifestedCard);
        assertThat(findPermanent(player1, "Cursed Windbreaker").getAttachedTo())
                .isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestsAndAttachesWithOnlyOneCardInLibrary() {
        Card manifestedCard = new Forest();
        harness.setHand(player1, List.of(new CursedWindbreaker()));
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(findPermanent(player1, "Cursed Windbreaker").getAttachedTo())
                .isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryLeavesEquipmentUnattached() {
        harness.setHand(player1, List.of(new CursedWindbreaker()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Cursed Windbreaker").getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void reequippingRemovesFlyingFromThePreviousCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new CursedWindbreaker());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void turningManifestedCreatureFaceUpKeepsEquipmentAndFlying() {
        Card manifestedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new CursedWindbreaker()));
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(findPermanent(player1, "Cursed Windbreaker").getAttachedTo())
                .isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
    }

    @Test
    void entryTriggerStillManifestsAfterEquipmentLeavesBattlefield() {
        Card manifestedCard = new Forest();
        harness.setHand(player1, List.of(new CursedWindbreaker()));
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Cursed Windbreaker");
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, equipment);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(manifestedCard);
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isFalse();
        harness.assertNotOnBattlefield(player1, "Cursed Windbreaker");
        harness.assertInGraveyard(player1, "Cursed Windbreaker");
    }
}
