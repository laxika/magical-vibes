package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieTrailblazer.class, CabalCoffers.class, SengirVampire.class})
class ZombieTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Zombie makes the target land a Swamp until end of turn")
    void makesTargetLandSwampUntilEndOfTurn() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CabalCoffers());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(trailblazer.isTapped()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.SWAMP);

        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK))
                .isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
    }

    @Test
    @DisplayName("Tapping a Zombie gives the target creature swampwalk until end of turn")
    void grantsSwampwalkUntilEndOfTurn() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent target = addCreatureReady(player2, new SengirVampire());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(trailblazer.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, trailblazer, Keyword.SWAMPWALK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("A different untapped Zombie can be tapped to pay the ability")
    void allowsAnotherZombieToPay() {
        Permanent otherZombie = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CabalCoffers());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, 0, null, land.getId());
        harness.handlePermanentChosen(player1, otherZombie.getId());
        harness.passBothPriorities();

        assertThat(otherZombie.isTapped()).isTrue();
        assertThat(trailblazer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A non-Zombie cannot be tapped to pay the ability")
    void requiresAnUntappedZombieToPay() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CabalCoffers());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        Permanent nonZombie = addCreatureReady(player1, new SengirVampire());

        assertThat(trailblazer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonZombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("The land ability cannot target a creature")
    void landAbilityRequiresLandTarget() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, trailblazer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The swampwalk ability cannot target a land")
    void creatureAbilityRequiresCreatureTarget() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CabalCoffers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(trailblazer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking while the defending player controls a Swamp")
    void swampwalkPreventsBlockingWithSwamp() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent otherZombie = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent attacker = addCreatureReady(player1, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new SengirVampire());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.handlePermanentChosen(player1, trailblazer.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(otherZombie.isTapped()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SWAMPWALK)).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Swampwalk allows blocking when the defending player controls no Swamp")
    void swampwalkAllowsBlockingWithoutSwamp() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent attacker = addCreatureReady(player1, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new SengirVampire());

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SWAMPWALK)).isTrue();
        assertThat(trailblazer.isTapped()).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
