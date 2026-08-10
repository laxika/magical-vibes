package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IsochronScepterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can imprint an instant with mana value 2 or less")
    void etbImprintsEligibleInstant() {
        harness.setHand(player1, List.of(new IsochronScepter(), new LightningBolt(), new Cancel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lightning Bolt"));
        harness.assertInHand(player1, "Cancel");
    }

    @Test
    @DisplayName("Activation casts a copy of the imprinted card for no mana")
    void activationCastsCopy() {
        IsochronScepter scepterCard = new IsochronScepter();
        Fog fogCard = new Fog();
        gd.setImprintedCard(scepterCard, fogCard);
        harness.addToBattlefield(player1, scepterCard);
        gd.exiledCards.add(new ExiledCardEntry(fogCard, player1.getId(), scepterCard.getId()));
        Permanent scepter = findPermanent(player1, "Isochron Scepter");
        scepter.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Fog") && entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Fog"))
                .hasSize(1);
        assertThat(gd.getImprintedCard(scepter.getCard())).isNotNull();
    }
}
