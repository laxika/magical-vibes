package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Shyft.class)
class ShyftTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting and choosing one color replaces Shyft's colors indefinitely")
    void acceptingSingleColorReplacesIndefinitely() {
        Permanent shyft = addCreatureReady(player1, new Shyft());

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
        Permanent shyft = addCreatureReady(player1, new Shyft());

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, shyft))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("Choosing all five colors completes the choice without DONE")
    void choosingAllColorsCompletesAutomatically() {
        Permanent shyft = addCreatureReady(player1, new Shyft());

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.getEffectiveColors(gd, shyft))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Declining the may ability leaves Shyft's colors unchanged")
    void decliningLeavesColorsUnchanged() {
        Permanent shyft = addCreatureReady(player1, new Shyft());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("A later upkeep choice overrides the previous indefinite color set")
    void laterChoiceOverridesPrevious() {
        Permanent shyft = addCreatureReady(player1, new Shyft());

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.RED);

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A new Shyft object does not retain a previous object's color change")
    void colorChangeDoesNotCarryToNewObjectAfterLeavingBattlefield() {
        Permanent shyft = addCreatureReady(player1, new Shyft());

        triggerUpkeepAndAccept(player1);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, shyft)).containsExactly(CardColor.RED);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, shyft));
        Card returnedShyft = shyft.getCard();
        assertThat(gd.playerHands.get(player1.getId())).contains(returnedShyft);
        gd.playerHands.get(player1.getId()).remove(returnedShyft);
        Permanent newShyft = harness.enterBattlefieldAndReturn(player1, returnedShyft);
        assertThat(gqs.getEffectiveColors(gd, newShyft)).containsExactly(CardColor.BLUE);
    }

    private void triggerUpkeepAndAccept(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player, true);
        harness.passBothPriorities(); // resolve BecomeChosenColorsIndefinitelyEffect → color choice
    }
}
