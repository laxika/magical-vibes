package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HourOfNeed;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.cards.t.TempleOfMalady;
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

@CardUsed({BlindingFlare.class, PensiveMinotaur.class, TempleOfMalady.class, HourOfNeed.class})
class BlindingFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Makes each target creature unable to block this turn")
    void makesEachTargetCreatureUnableToBlock() {
        Permanent firstCreature = addCreatureReady(player2, new PensiveMinotaur());
        Permanent secondCreature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isCantBlockThisTurn()).isTrue();
        assertThat(secondCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Strive requires one additional red mana per additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstCreature = addCreatureReady(player2, new PensiveMinotaur());
        Permanent secondCreature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void canBeCastWithNoTargets() {
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    @Test
    @DisplayName("Can't-block effect wears off at end of turn")
    void cantBlockWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfMalady());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canTargetMoreThanNinetyNineCreatures() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> addCreatureReady(player2, new PensiveMinotaur()))
                .toList();
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 100);

        harness.castAndResolveSorcery(player1, 0, creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isCantBlockThisTurn);
    }

    @Test
    void affectsCreaturesControlledByEitherPlayerButNotUnchosenCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new PensiveMinotaur());
        Permanent firstOpponentCreature = addCreatureReady(player2, new PensiveMinotaur());
        Permanent secondOpponentCreature = addCreatureReady(player2, new PensiveMinotaur());
        Permanent unchosenCreature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(
                ownCreature.getId(), firstOpponentCreature.getId(), secondOpponentCreature.getId()));

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(firstOpponentCreature.isCantBlockThisTurn()).isTrue();
        assertThat(secondOpponentCreature.isCantBlockThisTurn()).isTrue();
        assertThat(unchosenCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void additionalTargetRequiresRedManaRatherThanGenericMana() {
        Permanent firstCreature = addCreatureReady(player2, new PensiveMinotaur());
        Permanent secondCreature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent creature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillAffectsRemainingTargetWhenAnotherTargetIsExiledInResponse() {
        Permanent exiledCreature = addCreatureReady(player2, new PensiveMinotaur());
        Permanent remainingCreature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new BlindingFlare()));
        harness.setHand(player2, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, List.of(exiledCreature.getId(), remainingCreature.getId()));
        harness.castAndResolveInstant(player2, 0, List.of(exiledCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(exiledCreature);
        assertThat(remainingCreature.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(remainingCreature.getId()))
                .hasSize(1)
                .allMatch(permanent -> !permanent.isCantBlockThisTurn());
    }
}
