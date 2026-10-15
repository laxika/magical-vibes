package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UthgardtFury.class, HillGiant.class, Shock.class})
class UthgardtFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and deals 4 damage to any target player")
    void entersAndDealsDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UthgardtFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player1, "Uthgardt Fury");
    }

    @Test
    @DisplayName("Keeps damage marked on opponents' creatures through cleanup")
    void keepsOpponentsCreatureDamageThroughCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UthgardtFury(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, ownCreature.getId());
        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);

        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The entering trigger can deal lethal damage to a creature")
    void enteringTriggerKillsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UthgardtFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Uthgardt Fury");
    }

    @Test
    @DisplayName("Damage persists across repeated cleanups and clears once Fury leaves")
    void damageClearsAfterFuryLeaves() {
        Permanent fury = harness.addToBattlefieldAndReturn(player1, new UthgardtFury());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.CLEANUP);
        TurnCleanupService cleanup = GameTestEngineContext.get().getBean(TurnCleanupService.class);
        cleanup.applyCleanupResets(gd);
        cleanup.applyCleanupResets(gd);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);

        GameTestEngineContext.get().getBean(PermanentRemovalService.class)
                .destroyPermanentToGraveyard(gd, fury);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        cleanup.applyCleanupResets(gd);

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Furies controlled by both players preserve damage on both players' creatures")
    void opposingFuriesPreserveBothPlayersDamage() {
        harness.addToBattlefield(player1, new UthgardtFury());
        harness.addToBattlefield(player2, new UthgardtFury());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, ownCreature.getId());
        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());

        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
    }
}
