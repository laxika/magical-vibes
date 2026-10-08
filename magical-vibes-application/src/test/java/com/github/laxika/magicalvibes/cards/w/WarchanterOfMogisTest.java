package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarchanterOfMogis.class, GrizzlyBears.class})
class WarchanterOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Warchanter of Mogis queues a target creature choice")
    void untappingQueuesTargetChoice() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        warchanter.tap();

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .contains(bears.getId());
    }

    @Test
    @DisplayName("The chosen creature you control gains intimidate until end of turn")
    void grantsIntimidateToChosenCreature() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Intimidate wears off at end of turn")
    void intimidateWearsOffAtEndOfTurn() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("The trigger cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        warchanter.tap();

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownBears.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Warchanter can target itself with its inspired ability")
    void canGrantIntimidateToItself() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, warchanter.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warchanter, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("An already untapped Warchanter does not trigger in the untap step")
    void alreadyUntappedDoesNotTrigger() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, warchanter, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("A target that changes controllers before resolution does not gain intimidate")
    void targetMustStillBeControlledAtResolution() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INTIMIDATE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The inspired ability resolves after its source leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        Permanent warchanter = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WarchanterOfMogis());
        warchanter.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(warchanter);
        gd.playerGraveyards.get(player1.getId()).add(warchanter.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INTIMIDATE)).isTrue();
    }
}
