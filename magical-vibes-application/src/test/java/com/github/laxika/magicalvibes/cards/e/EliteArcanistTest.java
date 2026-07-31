package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EliteArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB choice exiles an instant from hand and imprints it")
    void etbImprintsInstantFromHand() {
        harness.setHand(player1, List.of(new EliteArcanist(), new Fog()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Arcanist → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Fog"));
        harness.assertNotInHand(player1, "Fog");

        Permanent arcanist = findPermanent(player1, "Elite Arcanist");
        assertThat(gd.getImprintedCard(arcanist.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(arcanist.getCard()).getName()).isEqualTo("Fog");
    }

    @Test
    @DisplayName("Declining the ETB choice leaves the instant in hand")
    void decliningEtbLeavesInstantInHand() {
        harness.setHand(player1, List.of(new EliteArcanist(), new Fog()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Fog");
        Permanent arcanist = findPermanent(player1, "Elite Arcanist");
        assertThat(gd.getImprintedCard(arcanist.getCard())).isNull();
    }

    @Test
    @DisplayName("Activating copies the exiled card and casting the copy resolves it, leaving the original exiled")
    void activateCastsCopyAndKeepsOriginalExiled() {
        Permanent arcanist = imprintFogOnArcanist();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities(); // Resolve the ability → may-cast prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // The copy is on the stack as a copy, not as the exiled original.
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Fog") && entry.isCopy());

        harness.passBothPriorities(); // Resolve the copy

        // The copy ceased to exist and the imprinted original is still exiled and imprinted.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Fog"))
                .hasSize(1);
        assertThat(gd.getImprintedCard(arcanist.getCard())).isNotNull();
    }

    @Test
    @DisplayName("Declining the may-cast makes the copy cease to exist")
    void decliningMayCastDiscardsCopy() {
        imprintFogOnArcanist();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Fog"))
                .hasSize(1);
        harness.assertNotInGraveyard(player1, "Fog");
    }

    @Test
    @DisplayName("Cannot activate when nothing is exiled with Elite Arcanist")
    void cannotActivateWithoutImprint() {
        EliteArcanist arcanistCard = new EliteArcanist();
        harness.addToBattlefield(player1, arcanistCard);
        findPermanent(player1, "Elite Arcanist").setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card has been exiled with");
    }

    @Test
    @DisplayName("X must equal the mana value of the exiled card")
    void xMustEqualExiledManaValue() {
        imprintFogOnArcanist();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must equal the mana value of the imprinted card");
    }

    private Permanent imprintFogOnArcanist() {
        EliteArcanist arcanistCard = new EliteArcanist();
        Fog fogCard = new Fog();
        gd.setImprintedCard(arcanistCard, fogCard);
        harness.addToBattlefield(player1, arcanistCard);
        gd.exiledCards.add(new ExiledCardEntry(fogCard, player1.getId(), arcanistCard.getId()));
        Permanent arcanist = findPermanent(player1, "Elite Arcanist");
        arcanist.setSummoningSick(false);
        return arcanist;
    }
}
