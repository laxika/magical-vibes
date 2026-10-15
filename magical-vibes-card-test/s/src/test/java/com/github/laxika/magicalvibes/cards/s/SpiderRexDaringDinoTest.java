package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VultureSchemingScavenger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderRexDaringDino.class, Shock.class, VultureSchemingScavenger.class})
class SpiderRexDaringDinoTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {2}")
    void wardCountersUnpaidSpell() {
        Permanent spiderRex = addCreatureReady(player1, new SpiderRexDaringDino());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spiderRex.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Spider-Rex, Daring Dino");
    }

    @Test
    @DisplayName("Paying Ward {2} lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent spiderRex = addCreatureReady(player1, new SpiderRexDaringDino());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, spiderRex.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Spider-Rex, Daring Dino");
    }

    @Test
    void paidWardAllowsDamageAndConsumesMana() {
        Permanent spiderRex = addCreatureReady(player1, new SpiderRexDaringDino());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, spiderRex.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(spiderRex.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void decliningAffordableWardPreventsDamage() {
        Permanent spiderRex = addCreatureReady(player1, new SpiderRexDaringDino());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, spiderRex.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(spiderRex.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void oneManaCannotPayWardTwo() {
        Permanent spiderRex = addCreatureReady(player1, new SpiderRexDaringDino());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, spiderRex.getId());

        assertThat(spiderRex.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllerCanTargetSpiderRexWithoutPayingWard() {
        Permanent spiderRex = addCreatureReady(player1, new SpiderRexDaringDino());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, spiderRex.getId());

        assertThat(spiderRex.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void reachAllowsBlockingFlyingAttacker() {
        Permanent attacker = addCreatureReady(player1, new VultureSchemingScavenger());
        Permanent spiderRex = addCreatureReady(player2, new SpiderRexDaringDino());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(spiderRex.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Vulture, Scheming Scavenger");
    }

    @Test
    void trampleAccountsForDamageAlreadyMarkedOnBlocker() {
        Permanent attacker = addCreatureReady(player1, new SpiderRexDaringDino());
        Permanent blocker = addCreatureReady(player2, new VultureSchemingScavenger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, blocker.getId());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Vulture, Scheming Scavenger");
        harness.assertOnBattlefield(player1, "Spider-Rex, Daring Dino");
    }
}
