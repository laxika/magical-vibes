package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InquisitorsSnareTest extends BaseCardTest {

    // ===== Prevention =====

    @Test
    @DisplayName("Target attacking creature is prevented from dealing damage")
    void preventsDamage() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Prevented attacker deals no combat damage to the player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        castSnare(attacker);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    // ===== Conditional destroy =====

    @Test
    @DisplayName("Black or red creature is also destroyed")
    void destroysRedCreature() {
        Permanent attacker = addAttacker(player2, new HillGiant()); // red 3/3
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Non-black-non-red creature is prevented but not destroyed")
    void doesNotDestroyGreenCreature() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears()); // green
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Prevention wears off =====

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        castSnare(attacker);

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InquisitorsSnare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService()
                .playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castSnare(Permanent target) {
        harness.setHand(player1, List.of(new InquisitorsSnare()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(owner, card);
        Permanent attacker = gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(card.getName()))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
