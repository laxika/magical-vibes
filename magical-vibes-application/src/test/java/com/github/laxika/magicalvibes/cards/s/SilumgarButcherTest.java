package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientCarp;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarButcher.class, CrawWurm.class, GrizzlyBears.class, AncientCarp.class, Flatten.class})
class SilumgarButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Silumgar Butcher and the other creature on the battlefield")
    void decliningExploitDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castButcher();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Silumgar Butcher");
    }

    @Test
    @DisplayName("Exploiting a creature gives target creature -3/-3 until end of turn")
    void exploitingCreatureDebuffsTargetUntilEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        castButcher();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrificing Silumgar Butcher to its own exploit still debuffs a creature you control")
    void selfExploitCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientCarp());
        castButcher();
        harness.passBothPriorities();
        var butcherId = harness.getPermanentId(player1, "Silumgar Butcher");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, butcherId);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silumgar Butcher");
        harness.assertNotOnBattlefield(player1, "Silumgar Butcher");
        assertThat(target.getEffectivePower()).isEqualTo(-1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The exploit debuff puts a creature with zero toughness into its owner's graveyard")
    void exploitDebuffKillsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SilumgarButcher());
        castButcher();
        harness.passBothPriorities();
        var butcherId = harness.getPermanentId(player1, "Silumgar Butcher");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, butcherId);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silumgar Butcher");
        harness.assertInGraveyard(player2, "Silumgar Butcher");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Self-exploit completes even when no creature remains to target")
    void selfExploitWithoutTargets() {
        castButcher();
        harness.passBothPriorities();
        var butcherId = harness.getPermanentId(player1, "Silumgar Butcher");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, butcherId);

        harness.assertInGraveyard(player1, "Silumgar Butcher");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing the Butcher before exploit resolves allows sacrifice but prevents the debuff")
    void removedButcherDoesNotTriggerDebuff() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AncientCarp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        castButcher();
        harness.passBothPriorities();
        var butcherId = harness.getPermanentId(player1, "Silumgar Butcher");
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, butcherId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Silumgar Butcher");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInGraveyard(player1, "Ancient Carp");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castButcher() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SilumgarButcher(), "{4}{B}");
    }
}
