package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlchemistsVialTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersDrawsCard() {
        harness.setHand(player1, List.of(new AlchemistsVial()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Resolve the enters-the-battlefield trigger from the stack
        harness.passBothPriorities();

        // Hand: -1 for the cast artifact, +1 for the drawn card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the Vial and locks the target out of attacking and blocking")
    void abilityLocksTargetOutOfCombat() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alchemist's Vial");
        harness.assertInGraveyard(player1, "Alchemist's Vial");

        Permanent locked = findPermanent(player2, "Grizzly Bears");
        assertThat(locked.isCantAttackThisTurn()).isTrue();
        assertThat(locked.isCantBlockThisTurn()).isTrue();
        assertThat(harness.getAttackLegalityService().canAttack(gd, locked, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Other creatures are unaffected")
    void otherCreaturesUnaffected() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent other = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> !p.getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(other.isCantAttackThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isFalse();
    }
}
