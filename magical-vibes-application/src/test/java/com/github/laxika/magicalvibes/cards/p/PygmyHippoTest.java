package com.github.laxika.magicalvibes.cards.p;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PygmyHippo.class, Forest.class, Island.class, GrizzlyBears.class})
class PygmyHippoTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player1, new PygmyHippo());
    }

    private void attackUnblocked(Permanent attacker) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.BlockerDeclaration) {
            gs.declareBlockers(gd, player2, List.of());
        }
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting drains defending lands, prevents combat damage, and adds equal {C} at next main")
    void unblockedAcceptDrainsAndAddsColorlessAtNextMain() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent island = gd.playerBattlefields.get(player2.getId()).get(1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addAttacker();

        attackUnblocked(attacker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        harness.assertLife(player2, 20);

        // Auto-pass through the rest of combat drains the delayed mana onto the stack at
        // postcombat main (same path as Conduit of Storms).
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unspent defending mana present at resolution is counted toward the delayed {C}")
    void unspentManaAtResolutionCounted() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = addAttacker();
        attackUnblocked(attacker);
        // Mana empties across steps; add it while the may is open (still declare blockers).
        harness.addMana(player2, ManaColor.RED, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        // 3 unspent + 1 from Forest
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining leaves lands untapped and the Hippo deals combat damage")
    void unblockedDeclineKeepsDamage() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent attacker = addAttacker();

        attackUnblocked(attacker);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Blocked attacker does not trigger")
    void blockedNoTrigger() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addAttacker();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(
                gd,
                player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }
}
