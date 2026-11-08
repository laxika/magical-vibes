package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import com.github.laxika.magicalvibes.cards.g.GreaterGargadon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.b.BlazingTorch;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshnodTheUncaring.class, AetherSpellbomb.class, GrizzlyBears.class, ProdigalPyromancer.class,
        GreaterGargadon.class, Forest.class, BloodthroneVampire.class, BlazingTorch.class})
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

    @Test
    @DisplayName("An ability of a suspended creature card does not trigger Ashnod")
    void suspendedCreatureCardAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        GreaterGargadon gargadon = new GreaterGargadon();
        harness.setHand(player1, List.of(gargadon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateExiledAbility(player1, gargadon.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(GreaterGargadon.class);
        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCardTimeCounters).containsEntry(gargadon.getId(), 9);
    }

    @Test
    @DisplayName("Sacrificing another creature to Bloodthrone Vampire triggers Ashnod, and the copy resolves")
    void chosenSacrificeTriggersAndCopyResolves() {
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vampire), null, null);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(vampire.getPowerModifier()).isEqualTo(4);
        assertThat(vampire.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing Blazing Torch for its equipped creature's ability triggers Ashnod")
    void equipmentSacrificeTriggers() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new BlazingTorch());
        torch.setAttachedTo(bears.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null,
                player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(torch);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
