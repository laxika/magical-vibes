package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdwardKenway.class, RenegadeFreighter.class, TalasScout.class,
        GrizzlyBears.class, Divination.class})
class EdwardKenwayTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure for each tapped Assassin, Pirate, and Vehicle you control")
    void createsTreasureForEachTappedQualifyingPermanent() {
        Permanent edward = addCreatureReady(player1, new EdwardKenway());
        edward.tap();
        Permanent pirate = addCreatureReady(player1, new TalasScout());
        pirate.tap();
        Permanent vehicle = addPermanent(player1, new RenegadeFreighter());
        vehicle.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Vehicle combat damage exiles the damaged player's top card face down with play permission")
    void vehicleCombatDamageExilesTopCardFaceDown() {
        addCreatureReady(player1, new EdwardKenway());
        Permanent vehicle = addPermanent(player1, new RenegadeFreighter());
        vehicle.setAnimatedUntilEndOfTurn(true);
        vehicle.setAnimatedPower(3);
        vehicle.setAnimatedToughness(2);
        vehicle.setAttacking(true);
        vehicle.setAttackTarget(player2.getId());

        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Combat damage from a non-Vehicle does not trigger the exile ability")
    void nonVehicleCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new EdwardKenway());
        Permanent attacker = addCreatureReady(player1, new TalasScout());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
