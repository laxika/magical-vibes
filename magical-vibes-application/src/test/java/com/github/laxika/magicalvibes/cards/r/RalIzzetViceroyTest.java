package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MaximizeAltitude;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RalIzzetViceroy.class, Forest.class, GiantGrowth.class, GrizzlyBears.class,
        HillGiant.class, Island.class, Mountain.class, Plains.class, Shock.class,
        MaximizeAltitude.class, PaladinEnVec.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeRejectsLandTarget() {
        addReadyRal(5);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

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

    @Test
    void plusOneRequiresOneCardToHand() {
        addReadyRal(5);
        harness.setLibrary(player1, List.of(new Plains(), new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneWithOneLibraryCardPutsItIntoHand() {
        addReadyRal(5);
        Card onlyCard = new Plains();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void plusOneWithEmptyLibraryDoesNotDrawOrMill() {
        Permanent ral = addReadyRal(5);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void minusThreeIgnoresFaceDownExiledSpells() {
        addReadyRal(5);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(new Shock()));
        gd.exiledCards.add(new ExiledCardEntry(new MaximizeAltitude(), player1.getId(), null, true));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void minusThreeCountsSorceriesAtResolution() {
        addReadyRal(5);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.setGraveyard(player1, List.of(new MaximizeAltitude()));
        harness.setExile(player1, List.of(new MaximizeAltitude(), new Plains()));
        harness.setGraveyard(player2, List.of(new MaximizeAltitude()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void emblemCanDamageCreatureWithProtectionFromRed() {
        addReadyRal(8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new Mountain()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void emblemTriggersForSorceryBeforeItResolves() {
        addReadyRal(8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstDraw = new Plains();
        Card secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Mountain()));
        harness.setHand(player1, List.of(new MaximizeAltitude()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emblemDoesNotDrawWhenItsOnlyTargetLeavesBattlefield() {
        addReadyRal(8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card firstDraw = new Plains();
        Card secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void emblemDoesNotTriggerForOpponentInstant() {
        addReadyRal(8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Card libraryCard = new Plains();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyRal(int loyalty) {
        Permanent ral = harness.addToBattlefieldAndReturn(player1, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, loyalty);
        ral.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ral;
    }
}
