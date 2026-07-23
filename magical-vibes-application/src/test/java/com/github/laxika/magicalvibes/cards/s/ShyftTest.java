package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShyftTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting and choosing one color replaces Shyft's colors indefinitely")
    void acceptingSingleColorReplacesIndefinitely() {
        Permanent shyft = addShyft(player1);

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.RED);

        // Survives end-of-turn cleanup (indefinite).
        shyft.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Choosing several colors makes Shyft all of those colors")
    void multipleColorsReplaceColors() {
        Permanent shyft = addShyft(player1);

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, shyft))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("Declining the may ability leaves Shyft's colors unchanged")
    void decliningLeavesColorsUnchanged() {
        Permanent shyft = addShyft(player1);

        triggerUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("A later upkeep choice overrides the previous indefinite color set")
    void laterChoiceOverridesPrevious() {
        Permanent shyft = addShyft(player1);

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.RED);

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.GREEN);
    }

    // ===== Helpers =====

    private Permanent addShyft(Player player) {
        Permanent perm = new Permanent(new Shyft());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, firing the trigger
    }

    private void triggerUpkeepAndAccept(Player player) {
        triggerUpkeep(player);
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player, true);
        harness.passBothPriorities(); // resolve BecomeChosenColorsIndefinitelyEffect → color choice
    }
}
