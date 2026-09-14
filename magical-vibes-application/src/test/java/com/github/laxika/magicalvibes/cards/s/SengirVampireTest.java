package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChainersEdict;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
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

@CardUsed({SengirVampire.class, AvenTrooper.class, FieryTemper.class, ChainersEdict.class})
class SengirVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new AvenTrooper());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Aven Trooper");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, sengir)).isEqualTo(5);
    }

    @Test
    @CardUsed(SoulsFire.class)
    @DisplayName("Gets one +1/+1 counter for each creature it damaged that dies")
    void getsOneCounterPerDamagedCreatureThatDies() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent firstTarget = addCreatureReady(player2, new AvenTrooper());
        Permanent secondTarget = addCreatureReady(player2, new AvenTrooper());
        harness.setHand(player1, List.of(new SoulsFire(), new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(sengir.getId(), firstTarget.getId()));
        resolveAllTriggers();
        harness.castInstant(player1, 0, List.of(sengir.getId(), secondTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Aven Trooper"))
                .hasSize(2);
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(6);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, sengir)).isEqualTo(6);
    }

    @Test
    @DisplayName("Triggers when a creature damaged by Sengir Vampire dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());

        AvenTrooper toughBlocker = new AvenTrooper();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(7);
        Permanent blocker = addCreatureReady(player2, toughBlocker);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Aven Trooper");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Aven Trooper");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
    }

    @Test
    @CardUsed(SoulsFire.class)
    @DisplayName("Triggers when a creature it deals noncombat damage to dies")
    void triggersWhenDamagedCreatureDiesFromNoncombatDamage() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player2, new AvenTrooper());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(sengir.getId(), target.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Aven Trooper");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void doesNotTriggerWhenUndamagedCreatureDies() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        Permanent target = addCreatureReady(player2, new AvenTrooper());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Aven Trooper");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a damaged creature dies on a later turn")
    void doesNotTriggerWhenDamagedCreatureDiesOnLaterTurn() {
        Permanent sengir = addCreatureReady(player1, new SengirVampire());
        AvenTrooper toughBlocker = new AvenTrooper();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(7);
        Permanent blocker = addCreatureReady(player2, toughBlocker);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Aven Trooper");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChainersEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Aven Trooper");
        assertThat(sengir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
