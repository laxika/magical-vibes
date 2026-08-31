package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BirthrightBoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanTheFaeBlooded.class, BirthrightBoon.class, GrizzlyBears.class,
        HolyStrength.class, LeoninScimitar.class})
class KellanTheFaeBloodedTest extends BaseCardTest {

    @Test
    void otherCreaturesGetPowerForEachAuraAndEquipmentAttachedToKellan() {
        Permanent kellan = addCreatureReady(player1, new KellanTheFaeBlooded());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());
        aura.setAttachedTo(kellan.getId());
        equipment.setAttachedTo(kellan.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(5);
    }

    @Test
    void birthrightBoonSearchesForAnAuraOrEquipmentAndPutsItIntoHand() {
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        Card creature = new GrizzlyBears();
        KellanTheFaeBlooded card = new KellanTheFaeBlooded();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature, aura, equipment));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getType)
                .containsExactlyInAnyOrder(CardType.ENCHANTMENT, CardType.ARTIFACT);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, equipment);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
