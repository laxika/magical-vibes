package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Floodbringer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatronOfTheMoon.class, Forest.class, Island.class, Floodbringer.class})
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
}
