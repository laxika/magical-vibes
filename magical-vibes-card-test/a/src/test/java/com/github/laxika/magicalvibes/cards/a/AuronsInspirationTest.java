package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwarvenCastleGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuronsInspiration.class, DwarvenCastleGuard.class})
class AuronsInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all attacking creatures and not nonattacking creatures")
    void boostsAttackingCreatures() {
        Permanent ownAttacker = addCreatureReady(player1, new DwarvenCastleGuard());
        ownAttacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new DwarvenCastleGuard());

        harness.setHand(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(ownAttacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The defending player can boost the opponent's attackers")
    void defendingPlayerBoostsOpponentAttackers() {
        Permanent attacker = addCreatureReady(player2, new DwarvenCastleGuard());
        attacker.setAttacking(true);
        Permanent defender = addCreatureReady(player1, new DwarvenCastleGuard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(defender.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The affected creatures are determined on resolution and retain the boost after combat")
    void determinesAttackersOnResolution() {
        Permanent removedFromCombat = addCreatureReady(player1, new DwarvenCastleGuard());
        removedFromCombat.setAttacking(true);
        Permanent lateAttacker = addCreatureReady(player1, new DwarvenCastleGuard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        removedFromCombat.setAttacking(false);
        lateAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(removedFromCombat.getEffectivePower()).isEqualTo(2);
        assertThat(lateAttacker.getEffectivePower()).isEqualTo(4);
        lateAttacker.setAttacking(false);
        removedFromCombat.setAttacking(true);
        assertThat(lateAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(removedFromCombat.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves with no attackers and does not boost later attackers")
    void resolvesWithNoAttackers() {
        Permanent creature = addCreatureReady(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Auron's Inspiration");
        creature.setAttacking(true);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback boosts attackers and exiles the spell after resolving")
    void flashbackBoostsAndExiles() {
        Permanent attacker = addCreatureReady(player1, new DwarvenCastleGuard());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.setGraveyard(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Auron's Inspiration");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Auron's Inspiration"));
    }

    @Test
    @DisplayName("The attacking-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new DwarvenCastleGuard());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);
        assertThat(attacker.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }
}
