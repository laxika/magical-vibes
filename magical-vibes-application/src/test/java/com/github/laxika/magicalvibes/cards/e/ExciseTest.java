package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExciseTest extends BaseCardTest {

    @Test
    @DisplayName("The attacking creature's controller declines and the creature is exiled")
    void controllerDeclinesAndCreatureIsExiled() {
        Permanent attacker = addAttacker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        castExcise(2, attacker.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The attacking creature is exiled automatically when its controller cannot pay")
    void cannotPayAutomaticallyExilesCreature() {
        addAttacker(player1);
        castExcise(2, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attacking creature's controller can pay the announced X")
    void controllerPaysXAndCreatureStays() {
        Permanent attacker = addAttacker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        castExcise(2, attacker.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("With X equal to zero, the target is not exiled")
    void zeroXDoesNotExileTarget() {
        Permanent attacker = addAttacker(player1);
        castExcise(0, attacker.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Excise cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player1);
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Excise()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstantForX(player2, 0, 2, List.of(nonAttacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private void castExcise(int x, java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Excise()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        if (x > 0) {
            harness.addMana(player2, ManaColor.COLORLESS, x);
        }
        harness.castInstantForX(player2, 0, x, List.of(targetId));
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }
}
