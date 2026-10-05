package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Floodbringer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatronOfTheMoon.class, Forest.class, Island.class, Floodbringer.class, Mountain.class, ValakutTheMoltenPinnacle.class, SphereOfResistance.class})
class PatronOfTheMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two land cards from hand onto the battlefield tapped")
    void putsTwoLandsTapped() {
        harness.addToBattlefield(player1, new PatronOfTheMoon());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        Permanent island = findPermanent(player1, "Island");
        assertThat(forest.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can put one land onto the battlefield after declining the first offer")
    void putsSecondLandWhenFirstOfferDeclined() {
        harness.addToBattlefield(player1, new PatronOfTheMoon());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Only one land may be put onto the battlefield when the second offer is declined")
    void putsOnlyOneLandWhenSecondDeclined() {
        harness.addToBattlefield(player1, new PatronOfTheMoon());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Declining both offers leaves the lands in hand")
    void decliningLeavesLandsInHand() {
        harness.addToBattlefield(player1, new PatronOfTheMoon());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Moonfolk offering sacrifices a Moonfolk and pays the difference in mana costs")
    void castsWithMoonfolkOffering() {
        Permanent moonfolk = harness.addToBattlefieldAndReturn(player1, new Floodbringer());
        harness.setHand(player1, List.of(new PatronOfTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(moonfolk.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Moon");
        harness.assertNotOnBattlefield(player1, "Floodbringer");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Both Mountains enter together and each sees five other Mountains")
    void simultaneousMountainsTriggerValakutTwice() {
        harness.addToBattlefield(player1, new PatronOfTheMoon());
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Moonfolk offering still pays the cost increase from Sphere of Resistance")
    void offeringPaysSpellCostIncrease() {
        Permanent moonfolk = harness.addToBattlefieldAndReturn(player1, new Floodbringer());
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.setHand(player1, List.of(new PatronOfTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(moonfolk.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Moon");
        harness.assertInGraveyard(player1, "Floodbringer");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped Patron may activate and cannot put a nonland from hand onto the battlefield")
    void tappedPatronOnlyPutsLands() {
        Permanent patron = harness.addToBattlefieldAndReturn(player1, new PatronOfTheMoon());
        patron.tap();
        harness.setHand(player1, List.of(new Floodbringer(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInHand(player1, "Floodbringer");
        harness.assertNotOnBattlefield(player1, "Floodbringer");
        assertThat(patron.isTapped()).isTrue();
    }
}
