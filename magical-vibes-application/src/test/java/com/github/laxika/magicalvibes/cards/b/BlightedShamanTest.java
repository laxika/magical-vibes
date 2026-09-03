package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightedShaman.class, Forest.class, NobleElephant.class, Swamp.class})
class BlightedShamanTest extends BaseCardTest {

    // ===== {T}, Sacrifice a Swamp: Target creature gets +1/+1 =====

    @Test
    @DisplayName("Sacrificing a Swamp gives target creature +1/+1 and taps Blighted Shaman")
    void sacrificeSwampGivesPlusOne() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NobleElephant());

        // permanentIndex 0 = Shaman, abilityIndex 0 = sacrifice a Swamp; only 1 Swamp → auto-sacrifice
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getCard().getId()));
    }

    // ===== {T}, Sacrifice a creature: Target creature gets +2/+2 =====

    @Test
    @DisplayName("Sacrificing a creature gives target creature +2/+2")
    void sacrificeCreatureGivesPlusTwo() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        // Only creature player1 controls is the Shaman → the sacrifice cost auto-picks it.
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NobleElephant());

        // abilityIndex 1 = sacrifice a creature
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        // The Shaman was the sacrificed creature.
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(shaman.getCard().getId()));
    }

    @Test
    @DisplayName("The creature-sacrifice ability can sacrifice another creature and leave the Shaman on the battlefield")
    void sacrificeCreatureCanChooseAnotherCreature() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        Permanent fodder = addCreatureReady(player1, new NobleElephant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NobleElephant());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shaman).doesNotContain(fodder);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(fodder.getCard().getId()));
    }

    // ===== Boost wears off at end of turn =====

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NobleElephant());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Cost cannot be paid without a Swamp =====

    @Test
    @DisplayName("The Swamp ability cannot be activated without a Swamp to sacrifice")
    void cannotActivateSwampAbilityWithoutSwamp() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NobleElephant());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId())
        ).isInstanceOf(IllegalStateException.class);
        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Swamp ability cannot be activated with a different land subtype")
    void cannotActivateSwampAbilityWithForest() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NobleElephant());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId())
        ).isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Swamp ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent shaman = addCreatureReady(player1, new BlightedShaman());
        Permanent costSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent noncreatureTarget = harness.addToBattlefieldAndReturn(player1, new Swamp());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, noncreatureTarget.getId())
        ).isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(shaman, costSwamp, noncreatureTarget);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
