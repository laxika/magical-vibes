package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TezzeretCruelMachinist.class, GrizzlyBears.class, MindStone.class, SoulWarden.class, Shock.class})
class TezzeretCruelMachinistTest extends BaseCardTest {

    @Test
    void drawsACard() {
        Permanent tezzeret = addReadyTezzeret(4);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void animatesTargetArtifactUntilYourNextTurn() {
        addReadyTezzeret(4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isAnimatedUntilNextTurn()).isTrue();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    void putsAnyNumberOfHandCardsFaceDownAsArtifactCreatures() {
        Permanent tezzeret = addReadyTezzeret(7);
        harness.setHand(player1, List.of(new GrizzlyBears(), new MindStone()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<Permanent> faceDown = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .toList();
        assertThat(faceDown).hasSize(2);
        assertThat(faceDown).allSatisfy(permanent -> {
            assertThat(gqs.isArtifact(gd, permanent)).isTrue();
            assertThat(gqs.isCreature(gd, permanent)).isTrue();
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(5);
        });
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void animationExpiresBeforeYourNextUntapStep() {
        addReadyTezzeret(4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
    }

    @Test
    void animationUsesAbilityControllersNextTurnAfterControlChanges() {
        addReadyTezzeret(4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    void ultimateCanPutNoCardsOntoBattlefield() {
        addReadyTezzeret(7);
        Card card = new MindStone();
        harness.setHand(player1, List.of(card));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isFaceDown);
    }

    @Test
    void ultimateCanStopAfterChoosingSomeCards() {
        addReadyTezzeret(7);
        Card remaining = new MindStone();
        harness.setHand(player1, List.of(new GrizzlyBears(), remaining));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(Permanent::isFaceDown))
                .hasSize(1);
    }

    @Test
    void faceDownCardsTriggerOtherCreaturesEntryAbilities() {
        addReadyTezzeret(7);
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MindStone(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    void animationRejectsAnOpponentsArtifact() {
        addReadyTezzeret(4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animationRejectsANonartifact() {
        addReadyTezzeret(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ultimateAcceptsInstantCardsAsFaceDownCreatures() {
        addReadyTezzeret(7);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(Permanent::isFaceDown))
                .singleElement().satisfies(permanent -> {
                    assertThat(gqs.isArtifact(gd, permanent)).isTrue();
                    assertThat(gqs.isCreature(gd, permanent)).isTrue();
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(5);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(5);
                });
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyTezzeret(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new TezzeretCruelMachinist());
        permanent.setSummoningSick(false);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
