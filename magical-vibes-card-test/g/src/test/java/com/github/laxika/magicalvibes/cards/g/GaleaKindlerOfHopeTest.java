package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaleaKindlerOfHope.class, GrizzlyBears.class, LeoninScimitar.class,
        Rancor.class, SwiftfootBoots.class})
class GaleaKindlerOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast an Equipment from the top of the library and attach it to a creature you control")
    void castsEquipmentFromLibraryTopAndAttachesIt() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card equipment = new LeoninScimitar();
        harness.setLibrary(player1, List.of(equipment));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent scimitar = findPermanent(player1, "Leonin Scimitar");
        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot cast a non-Aura, non-Equipment card from the top of the library")
    void cannotCastOtherCardsFromLibraryTop() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting an Aura from the library does not trigger Galea's Equipment ability")
    void auraDoesNotTriggerEquipmentAbility() {
        Permanent galea = harness.addToBattlefieldAndReturn(player1, new GaleaKindlerOfHope());
        Rancor aura = new Rancor();
        harness.setLibrary(player1, List.of(aura));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromLibraryTop(player1, galea.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(aura);
    }

    @Test
    @DisplayName("An Aura from the library enchants its original target without an Equipment attachment choice")
    void auraResolvesWithoutEquipmentAttachmentChoice() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GaleaKindlerOfHope());
        harness.setLibrary(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFromLibraryTop(player1, opponentCreature.getId());
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanent(player1, "Rancor").getAttachedTo()).isEqualTo(opponentCreature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Equipment cast from hand does not gain a free attachment")
    void equipmentFromHandDoesNotAttach() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        harness.setHand(player1, List.of(new SwiftfootBoots()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Swiftfoot Boots").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Galea privately exposes even a noncastable top card to its controller")
    void topCardVisibleOnlyToController() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        harness.setLibrary(player1, List.of(new GaleaKindlerOfHope()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
    }

    @Test
    @DisplayName("Galea does not waive the mana cost of Equipment cast from the library")
    void equipmentFromLibraryStillRequiresMana() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        SwiftfootBoots equipment = new SwiftfootBoots();
        harness.setLibrary(player1, List.of(equipment));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Galea does not let Equipment be cast outside sorcery timing")
    void equipmentFromLibraryRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new GaleaKindlerOfHope());
        SwiftfootBoots equipment = new SwiftfootBoots();
        harness.setLibrary(player1, List.of(equipment));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.stack).isEmpty();
    }
}
