package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasConstructionDrone.class, UrzasMine.class, UrzasPowerPlant.class,
        UrzasTower.class, Forest.class, Murder.class})
class UrzasConstructionDroneTest extends BaseCardTest {

    @Test
    void entersAndConjuresTheThreeUrzasLands() {
        harness.setHand(player1, List.of(new UrzasConstructionDrone()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Urza's Mine", "Urza's Power Plant", "Urza's Tower");
    }

    @Test
    void attacksAndSeeksAnUrzasLand() {
        addReadyDrone();
        Card sought = new UrzasMine();
        harness.setLibrary(player1, List.of(sought, new Forest()));

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void diesAndSeeksAnUrzasLand() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new UrzasConstructionDrone());
        Card sought = new UrzasTower();
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, drone.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Urza's Construction Drone");
    }

    private Permanent addReadyDrone() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new UrzasConstructionDrone());
        drone.setSummoningSick(false);
        return drone;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
