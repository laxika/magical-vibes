package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaleaKindlerOfHope.class, GrizzlyBears.class, LeoninScimitar.class})
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

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();
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
}
