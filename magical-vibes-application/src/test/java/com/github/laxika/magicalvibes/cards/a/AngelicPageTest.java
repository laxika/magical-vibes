package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicPage.class, Forest.class, GrizzlyBears.class})
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
    @DisplayName("Does not boost a creature that stops blocking before resolution")
    void doesNotBoostTargetThatStopsBlockingBeforeResolution() {
        Permanent blocker = addAngelicPageAndCombatCreature(false, true, player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost a creature that stops attacking before resolution")
    void doesNotBoostTargetThatStopsAttackingBeforeResolution() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addAngelicPage();
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void cannotActivateWithoutTarget() {
        addAngelicPage();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }

    @Test
    @DisplayName("Taps Angelic Page when the ability is activated")
    void tapsOnActivation() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(findPermanent(player1, "Angelic Page").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when Angelic Page is already tapped")
    void cannotActivateWhenTapped() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);
        findPermanent(player1, "Angelic Page").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate before Angelic Page has lost summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AngelicPage());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
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

    private Permanent addAngelicPage() {
        return addCreatureReady(player1, new AngelicPage());
    }

    private Permanent addAngelicPageAndCombatCreature(boolean attacking, boolean blocking, Player controller) {
        addAngelicPage();
        Permanent creature = addCreatureReady(controller, new GrizzlyBears());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        harness.forceActivePlayer(player1);
        harness.forceStep(attacking ? TurnStep.DECLARE_ATTACKERS : TurnStep.DECLARE_BLOCKERS);
        return creature;
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it is attacking")
    void cannotTargetNoncreatureAttacker() {
        addAngelicPage();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
