package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuronsInspiration.class, GrizzlyBears.class})
class AuronsInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all attacking creatures and not nonattacking creatures")
    void boostsAttackingCreatures() {
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback boosts attackers and exiles the spell after resolving")
    void flashbackBoostsAndExiles() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setGraveyard(player1, List.of(new AuronsInspiration()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Auron's Inspiration");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Auron's Inspiration"));
    }

    @Test
    @DisplayName("The attacking-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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
