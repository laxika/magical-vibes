package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicPage.class, CoralMerfolk.class, Forest.class})
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
        Permanent target = addCreatureReady(player1, new CoralMerfolk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
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

    @Test
    @DisplayName("Does not boost a target that stops attacking before resolution")
    void targetMustStillBeAttackingWhenAbilityResolves() {
        Permanent attacker = addAngelicPageAndCombatCreature(true, false, player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addAngelicPage() {
        return addCreatureReady(player1, new AngelicPage());
    }

    private Permanent addAngelicPageAndCombatCreature(boolean attacking, boolean blocking, Player controller) {
        addAngelicPage();
        Permanent creature = addCreatureReady(controller, new CoralMerfolk());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        harness.forceActivePlayer(player1);
        harness.forceStep(attacking ? TurnStep.DECLARE_ATTACKERS : TurnStep.DECLARE_BLOCKERS);
        return creature;
    }
}
