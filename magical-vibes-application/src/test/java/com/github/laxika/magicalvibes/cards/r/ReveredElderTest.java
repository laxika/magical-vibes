package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.COLORLESS;
import static org.assertj.core.api.Assertions.assertThat;

class ReveredElderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability adds a prevention shield to Revered Elder")
    void activationAddsPreventionShield() {
        Permanent elder = addReadyElder();
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elder.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The next 1 damage to Revered Elder is prevented")
    void preventsNextDamage() {
        Permanent elder = addReadyElder();
        Permanent attacker = addReadyCreature(player2, 2, 2);
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(elder.getMarkedDamage()).isEqualTo(1);
        assertThat(elder.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent elder = addReadyElder();
        harness.addMana(player1, COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elder.getDamagePreventionShield()).isZero();
    }

    private Permanent addReadyElder() {
        return addReadyCreature(player1, new ReveredElder());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
