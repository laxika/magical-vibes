package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChiefOfTheEdge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArashinForemost.class, ChiefOfTheEdge.class})
class ArashinForemostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield grants another Warrior you control double strike")
    void enteringGrantsDoubleStrike() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());

        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking grants another Warrior you control double strike")
    void attackingGrantsDoubleStrike() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());
        Permanent foremost = harness.addToBattlefieldAndReturn(player1, new ArashinForemost());
        foremost.setSummoningSick(false);

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The trigger can target only another Warrior you control")
    void targetMustBeAnotherWarriorYouControl() {
        Permanent ownWarrior = harness.addToBattlefieldAndReturn(player1, new ChiefOfTheEdge());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new ChiefOfTheEdge());

        harness.enterBattlefieldAndReturn(player1, new ArashinForemost());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownWarrior.getId());
        assertThat(choice.validIds()).doesNotContain(opponentWarrior.getId());
    }
}
