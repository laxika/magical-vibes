package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
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

class PanopticMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Activation only offers an instant or sorcery matching X for imprint")
    void activationImprintsMatchingManaValueSpell() {
        PanopticMirror mirrorCard = new PanopticMirror();
        harness.addToBattlefield(player1, mirrorCard);
        harness.setHand(player1, List.of(new LightningBolt(), new Divination()));
        findPermanent(player1, "Panoptic Mirror").setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ImprintFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        Permanent mirror = findPermanent(player1, "Panoptic Mirror");
        assertThat(gd.getImprintedCard(mirror.getCard())).isSameAs(gd.getPlayerExiledCards(player1.getId()).stream()
                .filter(card -> card.getName().equals("Divination"))
                .findFirst()
                .orElseThrow());
        harness.assertInHand(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Upkeep can copy and cast the imprinted card without paying its mana cost")
    void upkeepCopiesAndCastsImprintedCard() {
        PanopticMirror mirrorCard = new PanopticMirror();
        Fog fogCard = new Fog();
        harness.addToBattlefield(player1, mirrorCard);
        Permanent mirror = findPermanent(player1, "Panoptic Mirror");
        gd.setImprintedCard(mirrorCard, fogCard);
        gd.exiledCards.add(new ExiledCardEntry(fogCard, player1.getId(), mirror.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Fog") && entry.isCopy());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Fog"))
                .hasSize(1);
    }
}
