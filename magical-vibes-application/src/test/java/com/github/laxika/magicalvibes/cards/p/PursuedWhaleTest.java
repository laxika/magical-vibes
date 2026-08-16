package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PursuedWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("When Pursued Whale enters, each opponent creates a 1/1 red Pirate token")
    void eachOpponentCreatesPirateToken() {
        castAndResolveWhale();

        List<Permanent> pirates = findPermanents(player2, "Pirate");
        assertThat(pirates).hasSize(1);
        assertThat(pirates.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(pirates.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(pirates.getFirst().getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(pirates.getFirst().getCard().getSubtypes()).contains(CardSubtype.PIRATE);
        assertThat(bls.canBlock(gd, pirates.getFirst())).isFalse();
    }

    @Test
    @DisplayName("Pirate tokens force all creatures their controller controls to attack")
    void pirateTokenForcesControllerCreaturesToAttack() {
        castAndResolveWhale();
        Permanent pirate = findPermanent(player2, "Pirate");
        pirate.setSummoningSick(false);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell targeting Pursued Whale costs {3} more")
    void opponentSpellTargetingWhaleCostsMore() {
        Permanent whale = addCreatureReady(player1, new PursuedWhale());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, whale.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    private void castAndResolveWhale() {
        harness.setHand(player1, List.of(new PursuedWhale()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
