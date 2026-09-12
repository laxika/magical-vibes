package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelbesPortal.class, BelbesPercher.class, RootwaterCommando.class, SealOfCleansing.class})
class BelbesPortalTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Belbe's Portal lets its controller choose a creature type")
    void choosesCreatureTypeAsItEnters() {
        Permanent portal = addChosenPortal();

        assertThat(portal.getChosenSubtype()).isEqualTo(CardSubtype.BIRD);
    }

    @Test
    @DisplayName("Ability offers only creature cards of the chosen type")
    void abilityOffersOnlyChosenType() {
        Permanent portal = addChosenPortal();
        harness.setHand(player1, List.of(new BelbesPercher(), new RootwaterCommando(), new SealOfCleansing()));
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
        harness.setHand(player1, List.of(new BelbesPercher(), new RootwaterCommando()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Belbe's Percher");
        harness.assertNotOnBattlefield(player1, "Rootwater Commando");
        harness.assertInHand(player1, "Rootwater Commando");
    }

    @Test
    @DisplayName("Declining the ability leaves the hand unchanged")
    void decliningAbilityLeavesHandUnchanged() {
        Permanent portal = addChosenPortal();
        harness.setHand(player1, List.of(new BelbesPercher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Belbe's Portal");
        harness.assertInHand(player1, "Belbe's Percher");
        assertThat(portal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability uses the chosen type after the Portal leaves before resolution")
    void usesChosenTypeAfterPortalLeavesBeforeResolution() {
        Permanent portal = addChosenPortal();
        harness.addToBattlefield(player2, new SealOfCleansing());
        harness.setHand(player1, List.of(new BelbesPercher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, portal.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Belbe's Portal");
        harness.assertInGraveyard(player1, "Belbe's Portal");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Belbe's Percher");
    }

    private Permanent addChosenPortal() {
        harness.setHand(player1, List.of(new BelbesPortal()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BIRD");
        return findPermanent(player1, "Belbe's Portal");
    }
}
