package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BishopOfBinding.class, SunSentinel.class, DuskLegionZealot.class, Bombard.class, Tarmogoyf.class})
class BishopOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new SunSentinel());
        castBishop(harness.getPermanentId(player2, "Sun Sentinel"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sun Sentinel"));
    }

    @Test
    @DisplayName("Exiled creature returns when Bishop of Binding leaves the battlefield")
    void exiledCreatureReturnsWhenBishopLeaves() {
        harness.addToBattlefield(player2, new SunSentinel());
        castBishop(harness.getPermanentId(player2, "Sun Sentinel"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Bombard()));
        harness.addMana(player2, ManaColor.RED, 3);
        UUID bishopId = harness.getPermanentId(player1, "Bishop of Binding");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bishopId);

        harness.assertOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Sun Sentinel"));
    }

    @Test
    @DisplayName("Attacking boosts a target Vampire by the exiled creature's power")
    void attackBoostsVampireByExiledPower() {
        Permanent vampire = addCreatureReady(player1, new DuskLegionZealot());
        harness.addToBattlefield(player2, new SunSentinel());
        castBishop(harness.getPermanentId(player2, "Sun Sentinel"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bishop = findPermanent(player1, "Bishop of Binding");
        bishop.setSummoningSick(false);
        int bishopIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bishop);
        declareAttackers(player1, List.of(bishopIndex));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attack trigger cannot target a non-Vampire creature")
    void attackTriggerCannotTargetNonVampire() {
        addCreatureReady(player1, new DuskLegionZealot());
        Permanent nonVampire = addCreatureReady(player1, new SunSentinel());
        harness.addToBattlefield(player2, new SunSentinel());
        castBishop(harness.getPermanentId(player2, "Sun Sentinel"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bishop = findPermanent(player1, "Bishop of Binding");
        bishop.setSummoningSick(false);
        int bishopIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bishop);
        declareAttackers(player1, List.of(bishopIndex));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonVampire.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves prevents exile")
    void leavingBeforeEnterTriggerPreventsExile() {
        harness.addToBattlefield(player2, new SunSentinel());
        castBishop(harness.getPermanentId(player2, "Sun Sentinel"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Bishop of Binding"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the attack trigger resolves makes X zero")
    void leavingBeforeAttackTriggerMakesBoostZero() {
        Permanent vampire = addCreatureReady(player1, new DuskLegionZealot());
        harness.addToBattlefield(player2, new SunSentinel());
        castBishop(harness.getPermanentId(player2, "Sun Sentinel"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent bishop = findPermanent(player1, "Bishop of Binding");
        bishop.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bishop)));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vampire.getId());

        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, bishop.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sun Sentinel");
        assertThat(vampire.getPowerModifier()).isZero();
        assertThat(vampire.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost ignores modifiers the exiled creature had on the battlefield")
    void boostUsesPowerInExileAndCanTargetOpponentsVampire() {
        Permanent vampire = addCreatureReady(player2, new DuskLegionZealot());
        harness.addToBattlefield(player2, new SunSentinel());
        Permanent sentinel = findPermanent(player2, "Sun Sentinel");
        sentinel.setPowerModifier(5);
        castBishop(sentinel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent bishop = findPermanent(player1, "Bishop of Binding");
        bishop.setSummoningSick(false);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Characteristic-defining power is evaluated in exile when the attack trigger resolves")
    void boostUsesCharacteristicDefiningPowerInExile() {
        Permanent vampire = addCreatureReady(player1, new DuskLegionZealot());
        harness.setGraveyard(player1, List.of(new SunSentinel()));
        harness.addToBattlefield(player2, new Tarmogoyf());
        castBishop(harness.getPermanentId(player2, "Tarmogoyf"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent bishop = findPermanent(player1, "Bishop of Binding");
        bishop.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bishop)));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.setGraveyard(player2, List.of(new Bombard()));
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
    }

    private void castBishop(UUID targetId) {
        harness.setHand(player1, List.of(new BishopOfBinding()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
