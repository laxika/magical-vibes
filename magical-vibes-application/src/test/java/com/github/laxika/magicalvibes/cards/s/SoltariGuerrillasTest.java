package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoltariGuerrillasTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage to the opponent is dealt to the target creature instead")
    void redirectsCombatDamageToTargetCreature() {
        Permanent guerrillas = addReady(player1, new SoltariGuerrillas());
        Permanent destination = addReadyStats(player2, 2, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, indexOf(player1, guerrillas), null, destination.getId());
        harness.passBothPriorities();

        attackUnblocked(guerrillas);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destination);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(destination.getCard());
    }

    @Test
    @DisplayName("Only the next combat damage event is redirected; later damage hits the opponent")
    void redirectsOnlyTheNextDamageEvent() {
        Permanent guerrillas = addReady(player1, new SoltariGuerrillas());
        Permanent destination = addReadyStats(player2, 4, 4);

        harness.activateAbility(player1, indexOf(player1, guerrillas), null, destination.getId());
        harness.passBothPriorities();

        attackUnblocked(guerrillas);
        assertThat(gd.sourceNextCombatDamageToOpponentRedirectShields).isEmpty();

        int lifeBefore = gd.getLife(player2.getId());
        attackUnblocked(guerrillas);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("A player is an illegal target for the ability")
    void playerCannotBeTargeted() {
        Permanent guerrillas = addReady(player1, new SoltariGuerrillas());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, guerrillas), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent guerrillas = addReady(player1, new SoltariGuerrillas());
        Permanent destination = addReadyStats(player2, 4, 4);

        harness.activateAbility(player1, indexOf(player1, guerrillas), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.sourceNextCombatDamageToOpponentRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextCombatDamageToOpponentRedirectShields).isEmpty();
    }

    private void attackUnblocked(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
