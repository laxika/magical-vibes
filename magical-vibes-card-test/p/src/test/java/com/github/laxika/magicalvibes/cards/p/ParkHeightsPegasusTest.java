package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.j.Jackhammer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParkHeightsPegasus.class, CivicGardener.class, Jackhammer.class})
class ParkHeightsPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card after two creatures enter under its controller's control")
    void drawsAfterTwoCreaturesEnterUnderYourControl() {
        addAttackingPegasus();
        enterPermanents(player1, new CivicGardener(), new CivicGardener());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw after fewer than two creatures enter under its controller's control")
    void doesNotDrawAfterFewerThanTwoCreaturesEnter() {
        addAttackingPegasus();
        enterPermanents(player1, new CivicGardener());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Counts only creatures that entered under its controller's control")
    void ignoresCreatureEntriesUnderAnOpponentControl() {
        addAttackingPegasus();
        enterPermanents(player1, new CivicGardener());
        enterPermanents(player2, new CivicGardener());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Checks the creature-entry condition when the trigger resolves")
    void checksConditionOnResolution() {
        addAttackingPegasus();
        enterPermanents(player1, new CivicGardener());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        enterPermanents(player1, new CivicGardener());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Noncreature entries do not satisfy the creature-entry condition")
    void ignoresNoncreatureEntries() {
        addAttackingPegasus();
        enterPermanents(player1, new CivicGardener(), new Jackhammer());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Creatures that left the battlefield still count as having entered")
    void countsCreaturesThatHaveLeftTheBattlefield() {
        enterPermanents(player1, new CivicGardener(), new CivicGardener());
        gd.playerGraveyards.get(player1.getId()).addAll(
                gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard).toList());
        gd.playerBattlefields.get(player1.getId()).clear();
        addAttackingPegasus();
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The Pegasus itself counts if it entered this turn")
    void countsItsOwnEntry() {
        Permanent pegasus = harness.enterBattlefieldAndReturn(player1, new ParkHeightsPegasus());
        pegasus.setSummoningSick(false);
        pegasus.setAttacking(true);
        enterPermanents(player1, new CivicGardener());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addAttackingPegasus() {
        Permanent pegasus = addCreatureReady(player1, new ParkHeightsPegasus());
        pegasus.setAttacking(true);
        return pegasus;
    }

    private void enterPermanents(Player player, Card... cards) {
        for (Card card : cards) {
            harness.enterBattlefieldAndReturn(player, card);
        }
    }
}
