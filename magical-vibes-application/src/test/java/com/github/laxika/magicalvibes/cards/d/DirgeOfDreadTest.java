package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DirgeOfDread.class, GrizzlyBears.class})
class DirgeOfDreadTest extends BaseCardTest {

    @Test
    void allCreaturesGainFearUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FEAR)).isFalse();
    }

    @Test
    void cyclingMayGiveTargetCreatureFearBeforeDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
        harness.assertInGraveyard(player1, "Dirge of Dread");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void cyclingWithoutTargetStillDrawsACard() {
        harness.setHand(player1, List.of(new DirgeOfDread()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dirge of Dread");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
