package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindwhipSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain the discard ability")
    void grantsAbilityToAllSlivers() {
        Permanent mindwhipSliver = addCreatureReady(player1, new MindwhipSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, mindwhipSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new MindwhipSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Activating the granted ability sacrifices that Sliver and makes the target discard at random")
    void sacrificesSliverAndTargetDiscards() {
        addCreatureReady(player1, new MindwhipSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherSliver);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The granted ability cannot be activated at instant speed")
    void cannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new MindwhipSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherSliver);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
