package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnearthlyBlizzard.class, SenseisDiviningTop.class, WanderingOnes.class})
class UnearthlyBlizzardTest extends BaseCardTest {

    @Test
    @DisplayName("Up to three target creatures can't block this turn")
    void threeTargetsCantBlock() {
        Permanent creature1 = addCreatureReady(player2, new WanderingOnes());
        Permanent creature2 = addCreatureReady(player2, new WanderingOnes());
        Permanent creature3 = addCreatureReady(player2, new WanderingOnes());

        castUnearthlyBlizzard(List.of(creature1.getId(), creature2.getId(), creature3.getId()));

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
        assertThat(creature3.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target fewer than three creatures")
    void canTargetFewerThanThree() {
        Permanent creature1 = addCreatureReady(player2, new WanderingOnes());

        castUnearthlyBlizzard(List.of(creature1.getId()));

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can resolve with no targets")
    void canResolveWithNoTargets() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());

        castUnearthlyBlizzard(List.of());

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Can target your own creature without affecting untargeted creatures")
    void canTargetOwnCreatureWithoutAffectingUntargetedCreature() {
        Permanent ownCreature = addCreatureReady(player1, new WanderingOnes());
        Permanent untargetedCreature = addCreatureReady(player2, new WanderingOnes());

        castUnearthlyBlizzard(List.of(ownCreature.getId()));

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(untargetedCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target more than three creatures")
    void cannotTargetMoreThanThree() {
        Permanent c1 = addCreatureReady(player2, new WanderingOnes());
        Permanent c2 = addCreatureReady(player2, new WanderingOnes());
        Permanent c3 = addCreatureReady(player2, new WanderingOnes());
        Permanent c4 = addCreatureReady(player2, new WanderingOnes());

        prepareUnearthlyBlizzard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId(), c4.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new WanderingOnes());
        harness.addToBattlefield(player2, new SenseisDiviningTop());
        prepareUnearthlyBlizzard();

        UUID topId = harness.getPermanentId(player2, "Sensei's Divining Top");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(topId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeted creature actually cannot block")
    void targetedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new WanderingOnes());
        Permanent blocker = addCreatureReady(player2, new WanderingOnes());

        castUnearthlyBlizzard(List.of(blocker.getId()));

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skips targets that left the battlefield before resolution")
    void skipsRemovedTargets() {
        Permanent creature1 = addCreatureReady(player2, new WanderingOnes());
        Permanent creature2 = addCreatureReady(player2, new WanderingOnes());

        prepareUnearthlyBlizzard();
        harness.castSorcery(player1, 0, List.of(creature1.getId(), creature2.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(creature1);

        harness.passBothPriorities();

        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());

        castUnearthlyBlizzard(List.of(creature.getId()));

        harness.assertInGraveyard(player1, "Unearthly Blizzard");
    }

    private void castUnearthlyBlizzard(List<UUID> targetIds) {
        prepareUnearthlyBlizzard();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void prepareUnearthlyBlizzard() {
        harness.setHand(player1, List.of(new UnearthlyBlizzard()));
        harness.addMana(player1, ManaColor.RED, 3);
    }
}
