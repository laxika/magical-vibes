package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KahoMinamoHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability exiles up to three instant cards")
    void searchesForUpToThreeInstants() {
        harness.setLibrary(player1, List.of(
                new LightningBolt(), new Shock(), new GiantGrowth(), new DarkRitual(), new Divination()));
        harness.setHand(player1, List.of(new KahoMinamoHistorian()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(4);
        assertThat(search.params().remainingCount()).isEqualTo(3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent kaho = findPermanent(player1, "Kaho, Minamo Historian");
        assertThat(gd.getCardsExiledByPermanent(kaho.getId())).hasSize(3);
        assertThat(gd.getCardsExiledByPermanent(kaho.getId()))
                .allMatch(card -> card.hasType(CardType.INSTANT));
    }

    @Test
    @DisplayName("The activated ability free-casts exactly one exiled instant with the chosen mana value")
    void castsOneExiledInstantWithExactManaValue() {
        Permanent kaho = harness.addToBattlefieldAndReturn(player1, new KahoMinamoHistorian());
        kaho.setSummoningSick(false);
        LightningBolt firstBolt = new LightningBolt();
        LightningBolt secondBolt = new LightningBolt();
        Card wrongManaValue = new Cancel();
        gd.addToExile(player1.getId(), firstBolt, kaho.getId());
        gd.addToExile(player1.getId(), secondBolt, kaho.getId());
        gd.addToExile(player1.getId(), wrongManaValue, kaho.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getCardsExiledByPermanent(kaho.getId()))
                .contains(secondBolt)
                .contains(wrongManaValue)
                .hasSize(2);
    }
}
