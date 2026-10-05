package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvaderParasite.class, Forest.class, Mountain.class, Swamp.class})
class InvaderParasiteTest extends BaseCardTest {


    @Test
    @DisplayName("ETB exiles target land and imprints it")
    void etbExilesTargetLand() {
        exileForest();

        harness.assertOnBattlefield(player1, "Invader Parasite");
        harness.assertNotOnBattlefield(player2, "Forest");

        // Verify the forest is exiled (in player2's exile zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }


    @Test
    @DisplayName("Deals 2 damage when opponent plays land with same name as exiled card")
    void triggersOnMatchingLand() {
        // Set up Invader Parasite with an imprinted Forest
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        // Manually imprint a Forest
        Forest imprintedForest = new Forest();
        imprint(parasite, imprintedForest);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Opponent plays a Forest
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not trigger when opponent plays land with different name")
    void doesNotTriggerOnDifferentLand() {
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        Forest imprintedForest = new Forest();
        imprint(parasite, imprintedForest);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Opponent plays a Mountain (different name from imprinted Forest)
        harness.setHand(player2, List.of(new Mountain()));
        harness.playLand(player2, 0);

        // No trigger should fire
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for controller's own matching lands")
    void doesNotTriggerForControllerLands() {
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        Forest imprintedForest = new Forest();
        imprint(parasite, imprintedForest);

        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Controller plays a Forest (same name as imprint)
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        // No trigger — only cares about opponents
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when no card is imprinted")
    void doesNotTriggerWithNoImprint() {
        // Invader Parasite with no imprinted card (e.g. ETB was countered)
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        // No trigger — nothing imprinted
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Invader Parasites imprinting same land name trigger separately")
    void twoParasitesStack() {
        InvaderParasite parasite1 = new InvaderParasite();
        InvaderParasite parasite2 = new InvaderParasite();
        harness.addToBattlefield(player1, parasite1);
        harness.addToBattlefield(player1, parasite2);
        imprint(parasite1, new Forest());
        imprint(parasite2, new Forest());

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        // Two triggers (one per Invader Parasite)
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Triggers each time opponent plays a matching land on separate turns")
    void triggersOnEachMatchingLand() {
        InvaderParasite parasite = new InvaderParasite();
        harness.addToBattlefield(player1, parasite);
        imprint(parasite, new Swamp());

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // First Swamp on first turn
        harness.setHand(player2, List.of(new Swamp()));
        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        // Reset lands-played-this-turn tracking for a new turn
        gd.landsPlayedThisTurn.put(player2.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Second Swamp on new turn
        harness.setHand(player2, List.of(new Swamp()));
        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Exiling a land enables the matching land trigger")
    void exileAbilityEnablesDamageTrigger() {
        exileForest();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("No matching land trigger after the exiled card leaves exile")
    void noTriggerAfterExiledCardLeavesExile() {
        exileForest();
        Card forest = gd.getPlayerExiledCards(player2.getId()).getFirst();
        gd.removeFromExile(forest.getId());
        harness.setGraveyard(player2, List.of(forest));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Changing control before the exile trigger resolves preserves the imprint")
    void imprintSurvivesControlChangeBeforeResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new InvaderParasite()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, 0, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();

        Permanent parasite = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(parasite);
        gd.playerBattlefields.get(player2.getId()).add(parasite);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    private void exileForest() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new InvaderParasite()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, 0, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void imprint(InvaderParasite parasite, Card land) {
        harness.setExile(player2, List.of(land));
        gd.setImprintedCard(parasite, land);
    }
}
