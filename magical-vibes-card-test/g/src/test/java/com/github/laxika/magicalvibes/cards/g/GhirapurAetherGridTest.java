package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.j.JaceVrynsProdigy;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhirapurAetherGrid.class, Ornithopter.class, SavannahLions.class, JaceVrynsProdigy.class, Disperse.class})
class GhirapurAetherGridTest extends BaseCardTest {

    private static final int PING_ABILITY = 0;

    @Test
    @DisplayName("Deals 1 damage to target player, tapping two artifacts as the cost")
    void pingsPlayer() {
        Permanent grid = addGrid(player1);
        Permanent artifact1 = addArtifact(player1);
        Permanent artifact2 = addArtifact(player1);
        prepareMainPhase();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(grid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature, killing a 1/1")
    void pingsCreature() {
        Permanent grid = addGrid(player1);
        addArtifact(player1);
        addArtifact(player1);
        prepareMainPhase();

        harness.addToBattlefield(player2, new SavannahLions());
        UUID victim = harness.getPermanentId(player2, "Savannah Lions");

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("Cannot activate without two untapped artifacts")
    void requiresTwoUntappedArtifacts() {
        Permanent grid = addGrid(player1);
        addArtifact(player1);
        Permanent tapped = addArtifact(player1);
        tapped.tap();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifacts are tapped before damage resolves and cannot pay for another activation")
    void paysCostBeforeResolution() {
        Permanent grid = addGrid(player1);
        Permanent first = addArtifact(player1);
        Permanent second = addArtifact(player1);
        prepareMainPhase();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Summoning-sick artifact creatures can pay the tap cost")
    void acceptsSummoningSickArtifacts() {
        Permanent grid = addGrid(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        prepareMainPhase();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Opponent's artifacts cannot pay the cost")
    void rejectsOpponentsArtifacts() {
        Permanent grid = addGrid(player1);
        Permanent own = addArtifact(player1);
        Permanent opposing = addArtifact(player2);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonartifact creatures cannot pay the cost")
    void rejectsNonartifactCreatures() {
        Permanent grid = addGrid(player1);
        Permanent artifact = addArtifact(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses two artifacts and can activate again with a different pair")
    void choosesArtifactsAndActivatesRepeatedly() {
        Permanent grid = addGrid(player1);
        Permanent first = addArtifact(player1);
        Permanent second = addArtifact(player1);
        Permanent third = addArtifact(player1);
        Permanent fourth = addArtifact(player1);
        prepareMainPhase();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(first.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        assertThat(fourth.isTapped()).isFalse();

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());
        assertThat(second.isTapped()).isTrue();
        assertThat(fourth.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(grid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate during the opponent's turn and target its own controller")
    void activatesDuringOpponentsTurn() {
        Permanent grid = addGrid(player1);
        addArtifact(player1);
        addArtifact(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Damage to a planeswalker removes one loyalty counter")
    void pingsPlaneswalker() {
        Permanent grid = addGrid(player1);
        addArtifact(player1);
        addArtifact(player1);
        JaceVrynsProdigy jaceCard = new JaceVrynsProdigy();
        Permanent jace = harness.addToBattlefieldAndReturn(player2, jaceCard);
        jace.setTransformed(true);
        jace.setCard(jaceCard.getBackFaceCard());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        prepareMainPhase();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The ability still deals damage after the Grid leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent grid = addGrid(player1);
        Permanent first = addArtifact(player1);
        Permanent second = addArtifact(player1);
        prepareMainPhase();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());
        harness.castAndResolveInstant(player2, 0, grid.getId());
        harness.assertNotOnBattlefield(player1, "Ghirapur Aether Grid");
        harness.assertInHand(player1, "Ghirapur Aether Grid");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An illegal target at resolution does not refund the artifact tap cost")
    void targetLeavingDoesNotRefundCost() {
        Permanent grid = addGrid(player1);
        Permanent first = addArtifact(player1);
        Permanent second = addArtifact(player1);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        prepareMainPhase();
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, victim.getId());
        harness.castAndResolveInstant(player2, 0, victim.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Savannah Lions");
        harness.assertNotInGraveyard(player2, "Savannah Lions");
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enchantment that is not a creature is not a legal damage target")
    void rejectsNoncreatureEnchantmentTarget() {
        Permanent grid = addGrid(player1);
        Permanent first = addArtifact(player1);
        Permanent second = addArtifact(player1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, grid), PING_ABILITY, null, grid.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addGrid(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GhirapurAetherGrid());
    }

    private Permanent addArtifact(Player player) {
        return addCreatureReady(player, new Ornithopter());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
