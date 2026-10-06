package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.z.ZooEscapees;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayFilletManRay.class, ZooEscapees.class})
class RayFilletManRayTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mutagen artifact token")
    void etbCreatesMutagenToken() {
        castRayFilletManRay();

        Permanent mutagen = findPermanent(player1, "Mutagen");
        assertThat(mutagen.getCard().isToken()).isTrue();
        assertThat(mutagen.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Mutagen token puts a +1/+1 counter on a target creature")
    void mutagenPutsCounterOnCreature() {
        castRayFilletManRay();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(mutagen), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability removes a +1/+1 counter and draws a card")
    void drawAbilityRemovesCounterAndDraws() {
        castRayFilletManRay();
        Permanent ray = findPermanent(player1, "Ray Fillet, Man Ray");
        ray.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(ray), 0, null, null);
        harness.passBothPriorities();

        assertThat(ray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Activated ability requires a +1/+1 counter on a creature you control")
    void drawAbilityRequiresCounter() {
        castRayFilletManRay();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(findPermanent(player1, "Ray Fillet, Man Ray")), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    void createdTokenHasMutagenSubtype() {
        castRayFilletManRay();

        assertThat(findPermanent(player1, "Mutagen").getCard().getSubtypes())
                .contains(CardSubtype.MUTAGEN);
    }

    @Test
    void mutagenCanTargetOpponentsCreature() {
        castRayFilletManRay();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZooEscapees());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(mutagen), 0, null, creature.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCannotBeActivatedDuringOpponentsTurn() {
        castRayFilletManRay();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent ray = findPermanent(player1, "Ray Fillet, Man Ray");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mutagen), 0, null, ray.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(ray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mutagenCannotBeActivatedOutsideMainPhase() {
        castRayFilletManRay();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent ray = findPermanent(player1, "Ray Fillet, Man Ray");
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mutagen), 0, null, ray.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void drawAbilityChoosesAnotherControlledCreatureAndPaysBeforeResolution() {
        castRayFilletManRay();
        Permanent ray = findPermanent(player1, "Ray Fillet, Man Ray");
        ray.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(ray), 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void drawAbilityWorksDuringOpponentsTurnWithoutTappingSource() {
        castRayFilletManRay();
        Permanent ray = findPermanent(player1, "Ray Fillet, Man Ray");
        ray.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ray.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(ray), 0, null, null);
        assertThat(ray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(ray.isTapped()).isTrue();
    }

    @Test
    void drawAbilityCannotUseOpponentsCountersOrArtifactCounters() {
        castRayFilletManRay();
        Permanent ray = findPermanent(player1, "Ray Fillet, Man Ray");
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ZooEscapees());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        mutagen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ray), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mutagen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castRayFilletManRay() {
        harness.castFromHand(player1, new RayFilletManRay(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
