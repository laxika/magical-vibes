package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvenFortress;
import com.github.laxika.magicalvibes.cards.e.ElvishFarmer;
import com.github.laxika.magicalvibes.cards.f.FeralThallid;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightSoil.class, ElvishFarmer.class, FeralThallid.class, ElvenFortress.class})
class NightSoilTest extends BaseCardTest {

    @Test
    void paysExileCostBeforeResolutionAndCanActivateAgainWithoutTapping() {
        var nightSoil = harness.addToBattlefieldAndReturn(player1, new NightSoil());
        ElvishFarmer first = new ElvishFarmer();
        ElvishFarmer second = new ElvishFarmer();
        FeralThallid third = new FeralThallid();
        FeralThallid fourth = new FeralThallid();
        harness.setGraveyard(player2, List.of(first, second, third, fourth));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(third, fourth);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(countPermanents(player1, "Saproling")).isZero();
        assertThat(nightSoil.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(third.getId(), fourth.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(nightSoil.isTapped()).isFalse();
    }

    @Test
    void rejectsDuplicateAndCrossGraveyardSelectionsWithoutPayingCosts() {
        harness.addToBattlefield(player1, new NightSoil());
        ElvishFarmer first = new ElvishFarmer();
        FeralThallid second = new FeralThallid();
        ElvishFarmer opponentCreature = new ElvishFarmer();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(countPermanents(player1, "Saproling")).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    void exilesTwoCreatureCardsFromOpponentGraveyardAndCreatesSaproling() {
        var nightSoil = harness.addToBattlefieldAndReturn(player1, new NightSoil());
        ElvishFarmer farmer = new ElvishFarmer();
        FeralThallid thallid = new FeralThallid();
        harness.setGraveyard(player2, List.of(farmer, thallid));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gs.canActivateAbility(gd, player1.getId(), nightSoil, 0,
                gd.playerManaPools.get(player1.getId()))).isTrue();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(farmer.getId(), thallid.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(farmer, thallid);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        var saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().getPower()).isEqualTo(1);
        assertThat(saproling.getCard().getToughness()).isEqualTo(1);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    void canUseControllerGraveyard() {
        harness.addToBattlefield(player1, new NightSoil());
        ElvishFarmer farmer = new ElvishFarmer();
        FeralThallid thallid = new FeralThallid();
        harness.setGraveyard(player1, List.of(farmer, thallid));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(farmer.getId(), thallid.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(farmer, thallid);
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    void exilesExactlyTwoChosenCreatureCardsFromAThreeCardGraveyard() {
        harness.addToBattlefield(player1, new NightSoil());
        ElvishFarmer firstFarmer = new ElvishFarmer();
        ElvishFarmer secondFarmer = new ElvishFarmer();
        FeralThallid thallid = new FeralThallid();
        harness.setGraveyard(player2, List.of(firstFarmer, secondFarmer, thallid));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstFarmer.getId(), thallid.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondFarmer);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(firstFarmer, thallid);
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    void canChooseTwoCreatureCardsFromMixedGraveyard() {
        harness.addToBattlefield(player1, new NightSoil());
        ElvenFortress fortress = new ElvenFortress();
        ElvishFarmer farmer = new ElvishFarmer();
        FeralThallid thallid = new FeralThallid();
        harness.setGraveyard(player2, List.of(fortress, farmer, thallid));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(farmer.getId(), thallid.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(fortress);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(farmer, thallid);
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    void requiresBothCardsToComeFromTheSameGraveyard() {
        var nightSoil = harness.addToBattlefieldAndReturn(player1, new NightSoil());
        FeralThallid opponentCreature = new FeralThallid();
        ElvishFarmer controllerCreature = new ElvishFarmer();
        harness.setGraveyard(player1, List.of(controllerCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gs.canActivateAbility(gd, player1.getId(), nightSoil, 0,
                gd.playerManaPools.get(player1.getId()))).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void rejectsNonCreatureCardsAsCostCards() {
        harness.addToBattlefield(player1, new NightSoil());
        ElvishFarmer farmer = new ElvishFarmer();
        FeralThallid thallid = new FeralThallid();
        ElvenFortress fortress = new ElvenFortress();
        harness.setGraveyard(player1, List.of(farmer, thallid, fortress));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(farmer.getId(), fortress.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(farmer, thallid, fortress);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
