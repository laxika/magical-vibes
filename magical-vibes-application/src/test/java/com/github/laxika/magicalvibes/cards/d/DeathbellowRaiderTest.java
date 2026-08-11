package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathbellowRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Deathbellow Raider must attack each combat if able")
    void mustAttackWhenAble() {
        addReadyRaider(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Activating Deathbellow Raider's regeneration grants a shield")
    void regeneratesItself() {
        addReadyRaider(player1);
        addRegenerationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Deathbellow Raider").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathbellow Raider's regeneration shield prevents lethal damage")
    void regenerationShieldPreventsDestruction() {
        Permanent raider = addReadyRaider(player1);
        addRegenerationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        raider.setBlocking(true);
        raider.addBlockingTarget(0);

        Permanent attacker = addReadyCreature(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deathbellow Raider");
        assertThat(findPermanent(player1, "Deathbellow Raider").getRegenerationShield()).isZero();
    }

    private Permanent addReadyRaider(Player player) {
        Permanent raider = new Permanent(new DeathbellowRaider());
        raider.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(raider);
        return raider;
    }

    private Permanent addReadyCreature(Player player, int power, int toughness) {
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears card =
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addRegenerationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
