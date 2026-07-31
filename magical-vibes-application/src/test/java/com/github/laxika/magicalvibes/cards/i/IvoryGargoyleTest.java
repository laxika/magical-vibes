package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IvoryGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, returns to the battlefield at the next end step")
    void diesThenReturnsAtNextEndStep() {
        killGargoyle(player1);

        harness.assertInGraveyard(player1, "Ivory Gargoyle");
        assertThat(findPermanentOrNull(player1, "Ivory Gargoyle")).isNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // advance to the end step, processing the delayed return

        assertThat(findPermanentOrNull(player1, "Ivory Gargoyle")).isNotNull();
        harness.assertNotInGraveyard(player1, "Ivory Gargoyle");
    }

    @Test
    @DisplayName("Dies, controller skips their next draw step")
    void diesThenControllerSkipsNextDrawStep() {
        killGargoyle(player1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UPKEEP → DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Only one draw step is skipped per death")
    void skipsOnlyOneDrawStep() {
        killGargoyle(player1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // skipped draw step

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // the following draw step draws normally

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Activated ability exiles it, so it never dies and never returns")
    void activatedAbilityExilesIt() {
        harness.addToBattlefield(player1, new IvoryGargoyle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Ivory Gargoyle")).isNull();
        harness.assertNotInGraveyard(player1, "Ivory Gargoyle");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Ivory Gargoyle"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Ivory Gargoyle")).isNull();
    }

    private void killGargoyle(Player player) {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player, new IvoryGargoyle());
        gargoyle.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities(); // resolve the delayed-return death trigger
        harness.passBothPriorities(); // resolve the skip-draw-step death trigger
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
