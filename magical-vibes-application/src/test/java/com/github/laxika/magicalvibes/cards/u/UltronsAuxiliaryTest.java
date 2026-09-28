package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Fleshgrafter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronsAuxiliary.class, Fleshgrafter.class, GrizzlyBears.class, LeoninScimitar.class,
        Naturalize.class, Shock.class})
class UltronsAuxiliaryTest extends BaseCardTest {

    @Test
    void anotherArtifactPutIntoYourGraveyardFromBattlefieldAddsCounter() {
        Permanent auxiliary = harness.addToBattlefieldAndReturn(player1, new UltronsAuxiliary());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent scimitar = findPermanent(player1, "Leonin Scimitar");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, scimitar.getId());
        harness.passBothPriorities();

        assertThat(auxiliary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(auxiliary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void artifactCardPutIntoYourGraveyardFromHandAddsCounter() {
        Permanent auxiliary = harness.addToBattlefieldAndReturn(player1, new UltronsAuxiliary());
        Permanent fleshgrafter = harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        fleshgrafter.setSummoningSick(false);
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(auxiliary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonArtifactsAndOpponentsArtifactsDoNotTrigger() {
        Permanent auxiliary = harness.addToBattlefieldAndReturn(player1, new UltronsAuxiliary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.setHand(player1, List.of(new Shock(), new Naturalize()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(auxiliary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();

        assertThat(auxiliary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
