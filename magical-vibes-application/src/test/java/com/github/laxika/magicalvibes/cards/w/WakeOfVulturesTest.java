package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WakeOfVulturesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature regenerates Wake of Vultures")
    void sacrificingCreatureRegenerates() {
        Permanent vultures = addCreatureReady(player1, new WakeOfVultures());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(vultures.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(fodder.getId()));
        harness.assertOnBattlefield(player1, "Wake of Vultures");
    }

    @Test
    @DisplayName("Regeneration shield saves Wake of Vultures from lethal combat damage")
    void regeneratesFromLethalDamage() {
        addCreatureReady(player1, new WakeOfVultures());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        Permanent vultures = findPermanent(player1, "Wake of Vultures");
        assertThat(vultures.getRegenerationShield()).isEqualTo(1);

        vultures.setBlocking(true);
        vultures.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wake of Vultures");
        Permanent regenerated = findPermanent(player1, "Wake of Vultures");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Without a shield Wake of Vultures dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent vultures = addCreatureReady(player1, new WakeOfVultures());

        vultures.setBlocking(true);
        vultures.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wake of Vultures");
    }
}
