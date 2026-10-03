package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicSkirmisher.class, MillennialGargoyle.class})
class AngelicSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, first strike is granted to all creatures you control")
    void grantsFirstStrikeToOwnCreatures() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent bear = addCreatureReady(player1, new MillennialGargoyle());
        Permanent opposingBear = addCreatureReady(player2, new MillennialGargoyle());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "First strike");

        assertThat(gqs.hasKeyword(gd, skirmisher, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Choosing vigilance grants vigilance until end of turn")
    void grantsVigilanceUntilEndOfTurn() {
        addCreatureReady(player1, new AngelicSkirmisher());
        Permanent bear = addCreatureReady(player1, new MillennialGargoyle());

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            advanceToCombat(player1);
            harness.passBothPriorities();
            harness.handleListChoice(player1, "Vigilance");
        });

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Triggers during an opponent's combat and can grant lifelink")
    void triggersDuringOpponentsCombat() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent bear = addCreatureReady(player1, new MillennialGargoyle());
        Permanent opposingBear = addCreatureReady(player2, new MillennialGargoyle());

        advanceToCombat(player2);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, skirmisher, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The keyword is chosen when the combat trigger resolves, after responses")
    void choosesKeywordAtResolution() {
        addCreatureReady(player1, new AngelicSkirmisher());
        Permanent creature = addCreatureReady(player1, new MillennialGargoyle());

        advanceToCombat(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only creatures present at resolution gain the chosen ability")
    void snapshotsCreaturesAtResolution() {
        addCreatureReady(player1, new AngelicSkirmisher());
        advanceToCombat(player1);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new MillennialGargoyle());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "First strike");
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new MillennialGargoyle());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Removing Angelic Skirmisher does not stop its trigger granting the ability")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent creature = addCreatureReady(player1, new MillennialGargoyle());
        advanceToCombat(player1);
        gd.playerBattlefields.get(player1.getId()).remove(skirmisher);
        gd.playerGraveyards.get(player1.getId()).add(skirmisher.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The source does not gain the keyword if an opponent controls it at resolution")
    void excludesSourceControlledByOpponentAtResolution() {
        Permanent skirmisher = addCreatureReady(player1, new AngelicSkirmisher());
        Permanent creature = addCreatureReady(player1, new MillennialGargoyle());
        advanceToCombat(player1);
        gd.playerBattlefields.get(player1.getId()).remove(skirmisher);
        gd.playerBattlefields.get(player2.getId()).add(skirmisher);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "First strike");

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, skirmisher, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
