package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParadoxEngineTest extends BaseCardTest {

    @Test
    void untapsAllNonlandPermanentsItsControllerControls() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        engine.tap();
        artifact.tap();
        creature.tap();
        land.tap();
        opponentCreature.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(engine.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerForAnOpponentCastingASpell() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        engine.tap();
        creature.tap();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(engine.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }
}
