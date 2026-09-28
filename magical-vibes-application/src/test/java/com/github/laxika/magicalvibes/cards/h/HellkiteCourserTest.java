package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteCourser.class, GrizzlyBears.class})
class HellkiteCourserTest extends BaseCardTest {

    @Test
    void mayPutCommanderOntoBattlefieldWithHasteUntilNextEndStep() {
        Card commander = commander();
        Permanent hellkite = harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteredCommander = findPermanentByCardId(commander.getId());
        assertThat(enteredCommander.isCommander()).isTrue();
        assertThat(gqs.hasKeyword(gd, enteredCommander, Keyword.HASTE)).isTrue();
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNull();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
    }

    @Test
    void mayDeclineCommanderEntry() {
        Card commander = commander();
        harness.enterBattlefieldAndReturn(player1, new HellkiteCourser());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanentByCardIdOrNull(commander.getId())).isNull();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
    }

    private Card commander() {
        Card commander = new GrizzlyBears();
        commander.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private Permanent findPermanentByCardIdOrNull(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
