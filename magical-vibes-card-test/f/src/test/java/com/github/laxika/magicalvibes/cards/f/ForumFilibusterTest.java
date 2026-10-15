package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.DragonScales;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForumFilibuster.class, DragonScales.class, Bonesplitter.class, GrizzlyBears.class,
        LightningGreaves.class, SwordsToPlowshares.class, FallenIdeal.class})
class ForumFilibusterTest extends BaseCardTest {

    @Test
    void createsInklingAndReturnsAuraAttachedToThatToken() {
        DragonScales aura = new DragonScales();
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of(aura));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        resolveAllTriggers();

        Permanent inkling = findPermanent(player1, "Inkling");
        Permanent returnedAura = findPermanent(player1, "Dragon Scales");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(inkling.getId());
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
    }

    @Test
    void canReturnEquipmentAttachedToThatToken() {
        Bonesplitter equipment = new Bonesplitter();
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of(equipment));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        resolveAllTriggers();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(findPermanent(player1, "Bonesplitter").getAttachedTo()).isEqualTo(inkling.getId());
    }

    @Test
    void mayDeclineReturningTheTarget() {
        DragonScales aura = new DragonScales();
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of(aura));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dragon Scales");
        assertThat(findPermanents(player1, "Inkling")).hasSize(1);
    }

    @Test
    void nonAuraAndNonEquipmentCardsAreNotValidTargets() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Inkling")).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void equipmentEntersUnattachedWhenTheInklingLeavesBeforeTheReturnResolves() {
        LightningGreaves equipment = new LightningGreaves();
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of(equipment));

        advanceToUpkeep(player1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        Permanent inkling = findPermanent(player1, "Inkling");
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, inkling.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Lightning Greaves");
        assertThat(findPermanent(player1, "Lightning Greaves").getAttachedTo()).isNull();
    }

    @Test
    void auraRemainsInGraveyardWhenTheInklingLeavesBeforeTheReturnResolves() {
        FallenIdeal aura = new FallenIdeal();
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of(aura));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        Permanent inkling = findPermanent(player1, "Inkling");
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, inkling.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fallen Ideal");
        harness.assertNotOnBattlefield(player1, "Fallen Ideal");
    }

    @Test
    void doesNotTriggerDuringTheOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ForumFilibuster());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    @Test
    void createsTokenWithAnEmptyGraveyardAndIgnoresTheOpponentsEquipment() {
        harness.addToBattlefield(player1, new ForumFilibuster());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new LightningGreaves()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Inkling")).hasSize(1);
        harness.assertInGraveyard(player2, "Lightning Greaves");
        harness.assertNotOnBattlefield(player1, "Lightning Greaves");
    }
}
