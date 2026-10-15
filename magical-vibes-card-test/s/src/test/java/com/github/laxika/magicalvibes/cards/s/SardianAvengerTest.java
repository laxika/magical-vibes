package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SardianAvenger.class, MindStone.class, Naturalize.class})
class SardianAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 on attack for the number of artifacts opponents control")
    void boostsForOpponentsArtifacts() {
        var avenger = addCreatureReady(player1, new SardianAvenger());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(avenger.getPowerModifier()).isEqualTo(2);
        assertThat(avenger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Deals 1 damage to the controller of an opponent's artifact when it is destroyed")
    void damagesArtifactControllerWhenDestroyed() {
        addCreatureReady(player1, new SardianAvenger());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mind Stone"));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when an artifact controlled by its controller is destroyed")
    void doesNotTriggerForOwnArtifact() {
        addCreatureReady(player1, new SardianAvenger());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mind Stone"));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
