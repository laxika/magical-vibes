package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SengirVampire.class, SuntailHawk.class, Shock.class, CruelEdict.class, SoulsFire.class,
        AirElemental.class, MahamotiDjinn.class})
class SengirVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        addCreatureReady(player2, new SuntailHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, sengir)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets one +1/+1 counter for each creature it damaged that dies")
    void getsOneCounterPerDamagedCreatureThatDies() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent firstTarget = addCreatureReady(player2, new SuntailHawk());
        Permanent secondTarget = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SoulsFire(), new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(sengir.getId(), firstTarget.getId()));
        resolveAllTriggers();
        harness.castInstant(player1, 0, List.of(sengir.getId(), secondTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Suntail Hawk"))
                .hasSize(2);
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(6);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, sengir)).isEqualTo(6);
    }

    @Test
    @DisplayName("Triggers when a creature damaged by Sengir Vampire dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());

        SuntailHawk toughBlocker = new SuntailHawk();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(6);
        Permanent blocker = addCreatureReady(player2, toughBlocker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Suntail Hawk");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
    }

    @Test
    @DisplayName("Triggers when a creature it deals noncombat damage to dies")
    void triggersWhenDamagedCreatureDiesFromNoncombatDamage() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(sengir.getId(), target.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the Sengir Vampire that dealt the damage gets the counter")
    void triggersOnlyForTheSengirVampireThatDealtDamage() {
        Permanent watchingSengir = addCreatureReady(player1, new SengirVampire());
        Permanent attackingSengir = addCreatureReady(player1, new SengirVampire());
        addCreatureReady(player2, new SuntailHawk());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(watchingSengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attackingSengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void doesNotTriggerWhenUndamagedCreatureDies() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player2, new SuntailHawk());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a damaged creature dies on a later turn")
    void doesNotTriggerWhenDamagedCreatureDiesOnLaterTurn() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        SuntailHawk toughBlocker = new SuntailHawk();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(6);
        Permanent blocker = addCreatureReady(player2, toughBlocker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Suntail Hawk");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers for a damaged creature controlled by the Vampire's controller")
    void triggersWhenFriendlyCreatureDies() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(sengir.getId(), target.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Suntail Hawk");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when the Vampire and the creature it damaged die simultaneously")
    void triggersWhenBothCreaturesDieSimultaneously() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Sengir Vampire");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dealing damage to a player does not trigger the counter ability")
    void doesNotTriggerWhenDamagingPlayer() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(sengir.getId(), player2.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger if the Vampire leaves before the damaged creature dies")
    void doesNotTriggerAfterVampireHasLeftBattlefield() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player2, new MahamotiDjinn());
        harness.setHand(player1, List.of(new SoulsFire(), new SoulsFire(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(sengir.getId(), target.getId()));
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Mahamoti Djinn");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, List.of(sengir.getId(), sengir.getId()));
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Sengir Vampire");

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Mahamoti Djinn");
        assertThat(gd.stack).isEmpty();
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
