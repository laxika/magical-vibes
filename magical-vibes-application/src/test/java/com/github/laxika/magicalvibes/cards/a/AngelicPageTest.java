package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AngelicPageTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an attacking creature +1/+1 until end of turn")
    void boostsAttackingCreature() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosts a blocking creature +1/+1 until end of turn")
    void boostsBlockingCreature() {
        Permanent blocker = addAngelicPageAndCombatCreature(false, true, player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addAngelicPage();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps Angelic Page when the ability is activated")
    void tapsOnActivation() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(findPermanent(player1, "Angelic Page").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    private void addAngelicPage() {
        harness.addToBattlefield(player1, new AngelicPage());
        findPermanent(player1, "Angelic Page").setSummoningSick(false);
    }

    private Permanent addAngelicPageAndCombatCreature(boolean attacking, boolean blocking, Player controller) {
        addAngelicPage();
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        harness.getGameData().playerBattlefields.get(controller.getId()).add(creature);
        harness.forceActivePlayer(player1);
        harness.forceStep(attacking ? TurnStep.DECLARE_ATTACKERS : TurnStep.DECLARE_BLOCKERS);
        return creature;
    }
}
