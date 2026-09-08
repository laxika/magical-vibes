package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RalIzzetViceroyTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts one of the top two cards into hand and the other into the graveyard")
    void plusOneSeparatesTopTwoCards() {
        Permanent ral = addReadyRal(5);
        Card chosen = new Shock();
        Card discarded = new GiantGrowth();
        Card libraryBottom = new Plains();
        harness.setLibrary(player1, List.of(chosen, discarded, libraryBottom));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryBottom);
    }

    @Test
    @DisplayName("-3 deals damage equal to own instant and sorcery cards in graveyard and exile")
    void minusThreeCountsOwnInstantAndSorceryCards() {
        Permanent ral = addReadyRal(5);
        Card graveyardInstant = new GiantGrowth();
        Card exiledInstant = new Shock();
        harness.setGraveyard(player1, List.of(graveyardInstant, new Plains()));
        harness.setExile(player1, List.of(exiledInstant));
        harness.setExile(player2, List.of(new Shock()));
        harness.addToBattlefield(player2, new HillGiant());
        Permanent target = findPermanent(player2, "Hill Giant");

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeRejectsLandTarget() {
        addReadyRal(5);
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 emblem deals 4 damage and draws two cards for each controller instant or sorcery")
    void minusEightEmblemDealsDamageAndDraws() {
        addReadyRal(8);
        Card firstDraw = new Plains();
        Card secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Mountain()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("-8 emblem does not trigger for a creature spell")
    void minusEightEmblemIgnoresCreatureSpells() {
        addReadyRal(8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addReadyRal(int loyalty) {
        Permanent ral = new Permanent(new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, loyalty);
        ral.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ral);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ral;
    }
}
