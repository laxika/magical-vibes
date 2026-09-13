package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LatullaKeldonOverseer;
import com.github.laxika.magicalvibes.cards.v.VintaraElephant;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MirrorStrike.class, LatullaKeldonOverseer.class, VintaraElephant.class})
class MirrorStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage from the target attacker to its controller")
    void redirectsTargetCombatDamageToItsController() {
        Permanent target = addAttacker(player2, player1, new VintaraElephant());
        int protectedLifeBefore = gd.getLife(player1.getId());
        int attackerControllerLifeBefore = gd.getLife(player2.getId());
        castMirrorStrike(target);

        assertThat(gd.getLife(player1.getId())).isEqualTo(protectedLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerControllerLifeBefore - 4);
    }

    @Test
    @DisplayName("Does not redirect noncombat damage from the target attacker")
    void doesNotRedirectNoncombatDamage() {
        Permanent target = addAttacker(player2, player1, new LatullaKeldonOverseer());
        castMirrorStrike(target);

        harness.setHand(player2, List.of(new VintaraElephant(), new VintaraElephant()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, battlefieldIndex(player2, target), 1, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Redirects combat damage only from the targeted attacker")
    void redirectsOnlyTargetedAttackerDamage() {
        Permanent target = addAttacker(player2, player1, new VintaraElephant());
        addAttacker(player2, player1, new VintaraElephant());
        int protectedLifeBefore = gd.getLife(player1.getId());
        int attackerControllerLifeBefore = gd.getLife(player2.getId());

        castMirrorStrike(target);

        assertThat(gd.getLife(player1.getId())).isEqualTo(protectedLifeBefore - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerControllerLifeBefore - 4);
    }

    @Test
    @DisplayName("Cannot target a blocked attacker")
    void cannotTargetBlockedAttacker() {
        Permanent target = addAttacker(player2, player1, new VintaraElephant());
        Permanent blocker = addCreatureReady(player1, new VintaraElephant());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(target.getId());

        harness.setHand(player1, List.of(new MirrorStrike()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMirrorStrike(Permanent target) {
        harness.setHand(player1, List.of(new MirrorStrike()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent attacker = addCreatureReady(controller, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
