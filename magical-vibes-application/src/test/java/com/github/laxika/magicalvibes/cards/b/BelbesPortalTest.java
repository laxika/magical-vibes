package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BelbesPortalTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Belbe's Portal lets its controller choose a creature type")
    void choosesCreatureTypeAsItEnters() {
        Permanent portal = addChosenPortal();

        assertThat(portal.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Ability offers only creature cards of the chosen type")
    void abilityOffersOnlyChosenType() {
        Permanent portal = addChosenPortal();
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        assertThat(portal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing a matching creature puts it onto the battlefield")
    void putsChosenTypeCreatureOntoBattlefield() {
        addChosenPortal();
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Llanowar Elves");
    }

    private Permanent addChosenPortal() {
        harness.setHand(player1, List.of(new BelbesPortal()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        return findPermanent(player1, "Belbe's Portal");
    }
}
