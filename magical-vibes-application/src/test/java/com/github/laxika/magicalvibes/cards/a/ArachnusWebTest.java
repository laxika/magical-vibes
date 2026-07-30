package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArachnusWebTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(web);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature's activated abilities cannot be activated")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elves);

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(elves.getId());
        gd.playerBattlefields.get(player2.getId()).add(web);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroyed at the beginning of the end step when enchanted creature's power is 4 or greater")
    void destroyedAtEndStepWhenPowerIsFourOrGreater() {
        Permanent elemental = new Permanent(new AirElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(web);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arachnus Web");
        harness.assertInGraveyard(player1, "Arachnus Web");
    }

    @Test
    @DisplayName("Survives the end step when enchanted creature's power is less than 4")
    void survivesEndStepWhenPowerIsBelowFour() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(web);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arachnus Web");
    }
}
