package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.Hovermyr;
import com.github.laxika.magicalvibes.cards.i.ImmolatingSouleater;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaultSkirge.class, Hovermyr.class, ImmolatingSouleater.class})
class VaultSkirgeTest extends BaseCardTest {

    @Test
    void castsWithBlackManaWithoutPayingLife() {
        harness.setHand(player1, List.of(new VaultSkirge()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vault Skirge");
        harness.assertLife(player1, 20);
    }

    @Test
    void castsWithOneGenericManaAndTwoLife() {
        harness.setHand(player1, List.of(new VaultSkirge()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vault Skirge");
        harness.assertLife(player1, 18);
    }

    @Test
    void canSpendOnlyBlackManaOnGenericCostAndPayLifeForPhyrexianSymbol() {
        harness.setHand(player1, List.of(new VaultSkirge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vault Skirge");
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotPayPhyrexianSymbolWithLessThanTwoLife() {
        harness.setHand(player1, List.of(new VaultSkirge()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Vault Skirge");
        harness.assertLife(player1, 1);
    }

    @Test
    void groundCreatureCannotBlockFlyingAttacker() {
        addCreatureReady(player1, new VaultSkirge());
        addCreatureReady(player2, new ImmolatingSouleater());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void unblockedCombatDamageGainsLifeForController() {
        Permanent attacker = addCreatureReady(player1, new VaultSkirge());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void flyingBlockerStopsPlayerDamageButLifelinkStillGainsLifeWhenSkirgeDies() {
        addCreatureReady(player1, new VaultSkirge());
        addCreatureReady(player2, new Hovermyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Vault Skirge");
        harness.assertOnBattlefield(player2, "Hovermyr");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingFlyingCreatureGainsLifeForDefendingControllerEvenWhenBothDie() {
        addCreatureReady(player1, new VaultSkirge());
        addCreatureReady(player2, new VaultSkirge());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Vault Skirge");
        harness.assertInGraveyard(player2, "Vault Skirge");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
    }
}
