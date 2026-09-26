package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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

@CardUsed({ArmoredSkyhunter.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class, Shock.class})
class ArmoredSkyhunterTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, it puts Auras and Equipment from the top six onto the battlefield")
    void attacksAndAttachesEquipmentToAControlledCreature() {
        Permanent skyhunter = addCreatureReady(player1, new ArmoredSkyhunter());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        Card nonmatching = new Shock();
        harness.setLibrary(player1, List.of(aura, equipment, nonmatching));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(aura.getId(), equipment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.validIds()).containsExactly(skyhunter.getId(), bear.getId());

        harness.handlePermanentChosen(player1, bear.getId());

        Permanent foundEquipment = findPermanent(player1, "Leonin Scimitar");
        assertThat(foundEquipment.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonmatching);
    }
}
