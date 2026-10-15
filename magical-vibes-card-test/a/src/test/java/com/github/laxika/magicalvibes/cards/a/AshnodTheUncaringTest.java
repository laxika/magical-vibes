package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshnodTheUncaring.class, AetherSpellbomb.class, GrizzlyBears.class, ProdigalPyromancer.class})
class AshnodTheUncaringTest extends BaseCardTest {

    @Test
    void mayCopyAbilityAfterSourceArtifactIsSacrificed() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void decliningCopyLeavesAbilityUncopied() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForAnAbilityThatDidNotSacrificeAPermanent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        int pyromancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer);
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
