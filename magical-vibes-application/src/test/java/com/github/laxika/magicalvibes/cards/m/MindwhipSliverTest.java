package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HornedSliver;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindwhipSliver.class, HornedSliver.class, HornedTurtle.class})
class MindwhipSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain the discard ability")
    void grantsAbilityToAllSlivers() {
        Permanent mindwhipSliver = addCreatureReady(player1, new MindwhipSliver());
        Permanent ownSliver = addCreatureReady(player1, new HornedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new HornedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, mindwhipSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new MindwhipSliver());
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());

        assertThat(gs.getEffectiveActivatedAbilities(gd, turtle)).isEmpty();
    }

    @Test
    @DisplayName("Activating the granted ability sacrifices that Sliver and makes the target discard at random")
    void sacrificesSliverAndTargetDiscards() {
        Permanent mindwhipSliver = addCreatureReady(player1, new MindwhipSliver());
        Permanent sourceSliver = addCreatureReady(player1, new HornedSliver());
        harness.setHand(player2, List.of(new HornedTurtle(), new HornedTurtle()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sourceSliver);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(mindwhipSliver)
                .doesNotContain(sourceSliver);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability can target its controller and sacrifices the activating Sliver")
    void targetsItsController() {
        Permanent sourceSliver = addCreatureReady(player1, new MindwhipSliver());
        harness.setHand(player1, List.of(new HornedTurtle(), new HornedTurtle()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sourceSliver);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void requiresAPlayerTarget() {
        Permanent sourceSliver = addCreatureReady(player1, new MindwhipSliver());
        Permanent permanentTarget = addCreatureReady(player2, new HornedTurtle());
        harness.setHand(player2, List.of(new HornedTurtle()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, permanentTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sourceSliver);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability requires two generic mana and does not partially pay an insufficient cost")
    void requiresTwoGenericMana() {
        Permanent sourceSliver = addCreatureReady(player1, new MindwhipSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sourceSliver);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability sacrifices its source even when the target has no cards in hand")
    void sacrificesSourceWithEmptyTargetHand() {
        Permanent sourceSliver = addCreatureReady(player1, new MindwhipSliver());
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sourceSliver);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Mindwhip Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent mindwhipSliver = addCreatureReady(player1, new MindwhipSliver());
        Permanent otherSliver = addCreatureReady(player1, new HornedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(mindwhipSliver);

        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).isEmpty();
    }

    @Test
    @DisplayName("The granted ability cannot be activated at instant speed")
    void cannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new MindwhipSliver());
        Permanent otherSliver = addCreatureReady(player1, new HornedSliver());
        harness.setHand(player2, List.of(new HornedTurtle()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherSliver);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
