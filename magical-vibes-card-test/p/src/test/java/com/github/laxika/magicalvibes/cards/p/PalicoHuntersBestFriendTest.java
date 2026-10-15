package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalicoHuntersBestFriend.class, LeoninScimitar.class, Pacifism.class, Shock.class})
class PalicoHuntersBestFriendTest extends BaseCardTest {

    @Test
    @DisplayName("When Palico attacks, it may select an Aura from the top six")
    void attacksAndPutsAuraOntoBattlefield() {
        Permanent palico = addCreatureReady(player1, new PalicoHuntersBestFriend());
        Card aura = new Pacifism();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(aura, shock));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pacifism");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(palico.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("When an Equipment is found, it may attach to a creature you control")
    void attacksAndAttachesEquipment() {
        Permanent palico = addCreatureReady(player1, new PalicoHuntersBestFriend());
        Card equipment = new LeoninScimitar();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(equipment, shock));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent foundEquipment = findPermanent(player1, "Leonin Scimitar");
        assertThat(foundEquipment.getAttachedTo()).isEqualTo(palico.getId());
    }
}
