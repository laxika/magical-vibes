package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoltenFirebird.class)
class MoltenFirebirdTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, returns to the battlefield at the next end step")
    void diesThenReturnsAtNextEndStep() {
        killFirebird(player1);

        harness.assertInGraveyard(player1, "Molten Firebird");
        assertThat(findPermanentOrNull(player1, "Molten Firebird")).isNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Molten Firebird")).isNotNull();
        harness.assertNotInGraveyard(player1, "Molten Firebird");
    }

    @Test
    @DisplayName("Dies, controller skips their next draw step")
    void diesThenControllerSkipsNextDrawStep() {
        killFirebird(player1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Activated ability exiles it, so it never dies and never returns")
    void activatedAbilityExilesIt() {
        harness.addToBattlefield(player1, new MoltenFirebird());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Molten Firebird")).isNull();
        harness.assertNotInGraveyard(player1, "Molten Firebird");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Molten Firebird"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Molten Firebird")).isNull();
    }

    private void killFirebird(Player player) {
        Permanent firebird = harness.addToBattlefieldAndReturn(player, new MoltenFirebird());
        firebird.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
