package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MarchOfOtherworldlyLight;
import com.github.laxika.magicalvibes.cards.n.NezumiBladeblesser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainflailCentipede.class, NezumiBladeblesser.class, MarchOfOtherworldlyLight.class})
class ChainflailCentipedeTest extends BaseCardTest {

    @Test
    void attackingUnattachedBoostsItself() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(centipede.getPowerModifier()).isEqualTo(2);
        assertThat(centipede.getToughnessModifier()).isZero();
    }

    @Test
    void attackingEquippedCreatureBoostsTheEquippedCreature() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new NezumiBladeblesser());
        centipede.setAttachedTo(creature.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(centipede.getPowerModifier()).isZero();
    }

    @Test
    void attachedCentipedeIsNotACreatureAndReconfigureCanUnattachIt() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new NezumiBladeblesser());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, centipede)).isFalse();
        assertThat(centipede.getAttachedTo()).isEqualTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(centipede.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, centipede)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent opponentCreature = addCreatureReady(player2, new NezumiBladeblesser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(centipede.getAttachedTo()).isNull();
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(centipede.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(centipede.getPowerModifier()).isZero();
    }

    @Test
    void equippedAttackerStillGetsBoostWhenCentipedeIsExiledInResponse() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new NezumiBladeblesser());
        centipede.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        harness.castInstantForXWithDiscards(player2, 0, 3, List.of(centipede.getId()), List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Chainflail Centipede");
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void centipedeDoesNotGetBoostWhenEquippedAttackerIsExiledInResponse() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new NezumiBladeblesser());
        centipede.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        harness.castInstantForXWithDiscards(player2, 0, 3, List.of(creature.getId()), List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Nezumi Bladeblesser");
        assertThat(centipede.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, centipede)).isTrue();
        assertThat(centipede.getPowerModifier()).isZero();
    }

    @Test
    void reconfigureCanMoveDirectlyBetweenCreatures() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent first = addCreatureReady(player1, new NezumiBladeblesser());
        Permanent second = addCreatureReady(player1, new NezumiBladeblesser());
        centipede.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(centipede.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, centipede)).isFalse();
    }

    @Test
    void unattachedCentipedeCannotActivateUnattachAbility() {
        addCreatureReady(player1, new ChainflailCentipede());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothReconfigureAbilitiesAreRestrictedToSorceryTiming() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new NezumiBladeblesser());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        centipede.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
