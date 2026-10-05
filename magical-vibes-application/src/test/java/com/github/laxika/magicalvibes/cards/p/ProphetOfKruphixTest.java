package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThassasBounty;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
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

@CardUsed({ProphetOfKruphix.class, BronzeSable.class, Forest.class, ThassasBounty.class,
        TravelersAmulet.class})
class ProphetOfKruphixTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps creatures its controller controls during an opponent's untap step")
    void untapsControllerPermanentsDuringOpponentsUntapStep() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());
        Permanent tappedCreature = addCreatureReady(player1, new BronzeSable());
        tappedCreature.tap();

        harness.performUntapStep(player2);

        assertThat(tappedCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can cast creature spells during an opponent's turn")
    void canCastCreatureDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BronzeSable()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.passPriority(player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BronzeSable.class);
    }

    @Test
    @DisplayName("Does not let its controller cast noncreature spells during an opponent's turn")
    void doesNotGrantFlashToNoncreatureSpells() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThassasBounty()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Untaps itself and lands during each opponent's untap step")
    void untapsItselfAndLandsDuringOpponentsUntapStep() {
        Permanent prophet = harness.addToBattlefieldAndReturn(player1, new ProphetOfKruphix());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        prophet.tap();
        land.tap();

        harness.performUntapStep(player2);

        assertThat(prophet.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        land.tap();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap noncreature artifacts during an opponent's untap step")
    void doesNotUntapNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());
        artifact.tap();

        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap an opponent's creatures during its controller's untap step")
    void doesNotUntapOpponentsCreatures() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());
        Permanent creature = addCreatureReady(player2, new BronzeSable());
        creature.tap();

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not allow opponents to cast creatures at instant speed")
    void doesNotGrantFlashToOpponents() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BronzeSable()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Both permissions end when Prophet leaves the battlefield")
    void abilitiesEndWhenProphetLeavesBattlefield() {
        harness.addToBattlefield(player1, new ProphetOfKruphix());
        Permanent creature = addCreatureReady(player1, new BronzeSable());
        creature.tap();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof ProphetOfKruphix);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BronzeSable()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
