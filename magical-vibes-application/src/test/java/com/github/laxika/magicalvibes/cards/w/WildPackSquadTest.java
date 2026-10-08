package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildPackSquad.class})
class WildPackSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives up to one target creature first strike and vigilance")
    void beginningOfCombatGrantsKeywords() {
        harness.addToBattlefield(player1, new WildPackSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WildPackSquad());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The target may be declined")
    void targetMayBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WildPackSquad());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself and both keywords expire at cleanup")
    void canTargetItselfAndKeywordsExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WildPackSquad());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A target that leaves before resolution receives neither keyword")
    void removedTargetReceivesNeitherKeyword() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WildPackSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WildPackSquad());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isFalse();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("The trigger still grants both keywords if its source leaves before resolution")
    void triggerResolvesAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WildPackSquad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WildPackSquad());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, List.of(source.getCard()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new WildPackSquad());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
