package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.f.FestivalCrasher;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SeizeTheStorm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistflameReservoir.class, Island.class, Shock.class, SeizeTheStorm.class, FestivalCrasher.class})
class GeistflameReservoirTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a charge counter on Geistflame Reservoir")
    void castingInstantAddsChargeCounter() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing charge counters deals that much damage to any target")
    void removesCountersToDealDamage() {
        Permanent reservoir = addReadyReservoir(player1);
        reservoir.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(reservoir.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The library ability exiles the top card and grants play permission this turn")
    void exilesTopCardAndGrantsPlayPermission() {
        Permanent reservoir = addReadyReservoir(player1);
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(reservoir.isTapped()).isTrue();
    }

    private Permanent addReadyReservoir(Player player) {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player, new GeistflameReservoir());
        reservoir.setSummoningSick(false);
        return reservoir;
    }

    @Test
    void castingSorceryAddsCounterBeforeSpellResolves() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.setHand(player1, List.of(new SeizeTheStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void opponentsInstantDoesNotAddCounter() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void mayRemoveZeroCounters() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(reservoir.isTapped()).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    void countersArePaidBeforeResolutionAndDamageSurvivesSourceLeaving() {
        Permanent reservoir = addReadyReservoir(player1);
        reservoir.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 3, player2.getId());
        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(reservoir);
        gd.playerGraveyards.get(player1.getId()).add(reservoir.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void cannotRemoveMoreCountersThanAvailable() {
        Permanent reservoir = addReadyReservoir(player1);
        reservoir.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(reservoir.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledLandCanBePlayedThisTurn() {
        Permanent reservoir = addReadyReservoir(player1);
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void exiledSpellRequiresManaAndCastingItAddsCounter() {
        Permanent reservoir = addReadyReservoir(player1);
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void emptyLibraryAbilityStillResolvesAndTapsReservoir() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(reservoir.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void damageCanTargetACreature() {
        Permanent reservoir = addReadyReservoir(player1);
        reservoir.setCounterCount(CounterType.CHARGE, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 3, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Festival Crasher");
        harness.assertInGraveyard(player2, "Festival Crasher");
        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void castingCreatureDoesNotAddCounter() {
        Permanent reservoir = addReadyReservoir(player1);
        harness.setHand(player1, List.of(new FestivalCrasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(reservoir.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertOnBattlefield(player1, "Festival Crasher");
    }

    @Test
    void exiledSorceryStillRequiresSorceryTiming() {
        addReadyReservoir(player1);
        Card top = new SeizeTheStorm();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void unusedExiledCardStaysExiledButPermissionExpires() {
        addReadyReservoir(player1);
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
