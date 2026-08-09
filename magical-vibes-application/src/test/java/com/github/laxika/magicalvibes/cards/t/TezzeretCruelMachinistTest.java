package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent artifact = addPermanent(player1, new MindStone());

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

    private Permanent addReadyTezzeret(int loyalty) {
        Permanent permanent = addPermanent(player1, new TezzeretCruelMachinist());
        permanent.setSummoningSick(false);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
