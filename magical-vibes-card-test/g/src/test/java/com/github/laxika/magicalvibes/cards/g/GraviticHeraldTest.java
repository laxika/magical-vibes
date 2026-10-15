package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HinterlandHarbor;
import com.github.laxika.magicalvibes.cards.h.HydroponicsArchitect;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.ThoughtPartition;
import com.github.laxika.magicalvibes.cards.w.Worldweave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraviticHerald.class, GrizzlyBears.class, Plains.class,
        HydroponicsArchitect.class, HinterlandHarbor.class, ThoughtPartition.class, Worldweave.class})
class GraviticHeraldTest extends BaseCardTest {

    @Test
    void seeksNonlandPermanentWithManaValueThreeOrLessAndGrantsWarp() {
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(grizzlyBears, plains));
        harness.setHand(player1, List.of(new GraviticHerald()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(grizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).containsExactly(grizzlyBears.getId());

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent warpedGrizzly = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(warpedGrizzly.isCastWithWarp()).isTrue();
        assertThat(gd.spellWarpedThisTurn).isTrue();
    }

    @Test
    void excludesLandsNonpermanentsAndPermanentsAboveManaValueThree() {
        HydroponicsArchitect eligible = new HydroponicsArchitect();
        HinterlandHarbor land = new HinterlandHarbor();
        ThoughtPartition sorcery = new ThoughtPartition();
        GraviticHerald tooExpensive = new GraviticHerald();
        harness.setLibrary(player1, List.of(land, sorcery, eligible, tooExpensive));

        resolveHeraldEntry(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, sorcery, tooExpensive);
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).containsExactly(eligible.getId());
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void seeksNoncreaturePermanentAtManaValueThree() {
        Worldweave enchantment = new Worldweave();
        harness.setLibrary(player1, List.of(enchantment));

        resolveHeraldEntry(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).containsExactly(enchantment.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    void stillLosesLifeWhenLibraryContainsNoEligibleCard() {
        HinterlandHarbor land = new HinterlandHarbor();
        ThoughtPartition sorcery = new ThoughtPartition();
        GraviticHerald tooExpensive = new GraviticHerald();
        harness.setLibrary(player1, List.of(land, sorcery, tooExpensive));

        resolveHeraldEntry(player1);

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, sorcery, tooExpensive);
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).isEmpty();
    }

    @Test
    void stillLosesLifeWhenLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());

        resolveHeraldEntry(player1);

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).isEmpty();
    }

    @Test
    void seeksExactlyOneCardFromTheEnteringCreaturesControllersLibrary() {
        HydroponicsArchitect first = new HydroponicsArchitect();
        HydroponicsArchitect second = new HydroponicsArchitect();
        HydroponicsArchitect opponentsCard = new HydroponicsArchitect();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(opponentsCard));

        resolveHeraldEntry(player2);

        List<Card> hand = gd.playerHands.get(player2.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.getFirst()).isIn(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1).doesNotContain(hand.getFirst());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).containsExactly(hand.getFirst().getId());
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void grantedWarpExilesTheCastCreatureAtTheNextEndStep() {
        HydroponicsArchitect sought = new HydroponicsArchitect();
        harness.setLibrary(player1, List.of(sought));
        resolveHeraldEntry(player1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hydroponics Architect");

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertNotOnBattlefield(player1, "Hydroponics Architect");
        assertThat(gd.findExiledCard(sought.getId())).isNotNull();
    }

    @Test
    void uncastSoughtCardLosesGrantedWarpAtEndOfTurn() {
        HydroponicsArchitect sought = new HydroponicsArchitect();
        harness.setLibrary(player1, List.of(sought));
        resolveHeraldEntry(player1);

        assertThat(harness.getCastingCostService().canPayAlternateHandCast(gd, player1.getId(), sought))
                .isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(harness.getCastingCostService().canPayAlternateHandCast(gd, player1.getId(), sought))
                .isFalse();
    }

    private void resolveHeraldEntry(Player controller) {
        harness.setHand(controller, List.of());
        harness.enterBattlefieldAndReturn(controller, new GraviticHerald());
        harness.passBothPriorities();
    }
}
