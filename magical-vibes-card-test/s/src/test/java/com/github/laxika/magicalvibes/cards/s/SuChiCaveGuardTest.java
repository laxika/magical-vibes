package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuChiCaveGuard.class, WrathOfGod.class, ShootDown.class})
class SuChiCaveGuardTest extends BaseCardTest {

    @Test
    @DisplayName("When Su-Chi Cave Guard dies, it adds eight colorless mana that survives a phase transition")
    void diesAddsEightPersistentColorlessMana() {
        harness.addToBattlefield(player1, new SuChiCaveGuard());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Su-Chi Cave Guard");
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(8);
        assertThat(pool.getPersistentMana(ManaColor.COLORLESS)).isEqualTo(8);

        pool.add(ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(8);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    void vigilanceKeepsAttackerUntapped() {
        Permanent guard = addCreatureReady(player1, new SuChiCaveGuard());

        declareAttackers(List.of(0));

        assertThat(guard.isAttacking()).isTrue();
        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    void wardCountersSpellWhenOpponentHasOnlyThreeManaRemaining() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new SuChiCaveGuard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShootDown()));
        harness.addMana(player2, ManaColor.GREEN, 7);

        harness.castSorcery(player2, 0, guard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Su-Chi Cave Guard");
        harness.assertInGraveyard(player2, "Shoot Down");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void payingFourManaForWardAllowsExileWithoutDeathTrigger() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new SuChiCaveGuard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShootDown()));
        harness.addMana(player2, ManaColor.GREEN, 8);

        harness.castSorcery(player2, 0, guard.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Su-Chi Cave Guard");
        assertThat(gd.findExiledCard(guard.getCard().getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownSpellDoesNotTriggerWardOrDeathWhenExilingGuard() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new SuChiCaveGuard());
        harness.setHand(player1, List.of(new ShootDown()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, guard.getId());

        assertThat(gd.findExiledCard(guard.getCard().getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathManaUsesStackAndExpiresAtTurnEnd() {
        harness.addToBattlefield(player2, new SuChiCaveGuard());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(8);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
