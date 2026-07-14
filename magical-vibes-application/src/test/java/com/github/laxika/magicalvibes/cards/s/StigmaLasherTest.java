package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class StigmaLasherTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player can't gain life after Stigma Lasher deals damage to them")
    void damagedPlayerCantGainLife() {
        addAttackingLasher(player1);

        resolveCombat();
        // 2/2 dealt 2 combat damage.
        harness.assertLife(player2, 18);

        castAngelOfMercy(player2);

        // Life gain was prevented for the rest of the game.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevention persists after Stigma Lasher leaves the battlefield")
    void preventionPersistsAfterLasherLeaves() {
        addAttackingLasher(player1);

        resolveCombat();
        harness.assertLife(player2, 18);

        // Stigma Lasher leaves — the effect is a rest-of-game player state, not a static.
        gd.playerBattlefields.get(player1.getId()).clear();

        castAngelOfMercy(player2);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Undamaged opponent can still gain life")
    void undamagedPlayerCanStillGainLife() {
        addAttackingLasher(player1);

        resolveCombat();
        harness.assertLife(player2, 18);

        // player1 was never damaged by Stigma Lasher, so their life gain is unaffected.
        castAngelOfMercy(player1);

        harness.assertLife(player1, 23);
    }

    // ===== Helpers =====

    private void addAttackingLasher(Player player) {
        Permanent lasher = addReadyCreature(player, new StigmaLasher());
        lasher.setAttacking(true);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // resolve the damage trigger
    }

    private void castAngelOfMercy(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player, List.of(new AngelOfMercy()));
        harness.addMana(player, ManaColor.WHITE, 3);
        harness.addMana(player, ManaColor.COLORLESS, 2);

        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB gain life effect
    }
}
