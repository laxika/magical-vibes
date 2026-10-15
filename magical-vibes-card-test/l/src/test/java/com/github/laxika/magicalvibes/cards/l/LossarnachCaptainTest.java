package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LossarnachCaptain.class, EliteVanguard.class, GrizzlyBears.class, Conspiracy.class})
class LossarnachCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry taps a target creature an opponent controls")
    void ownEntryTapsOpponentCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCaptain();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Human entering taps a target creature an opponent controls")
    void anotherHumanEntryTapsOpponentCreature() {
        harness.addToBattlefield(player1, new LossarnachCaptain());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new EliteVanguard(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-Human creature entering does not trigger the tap ability")
    void nonHumanEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new LossarnachCaptain());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The upkeep trigger creates a 1/1 white Human Soldier token")
    void upkeepCreatesHumanSoldierToken() {
        harness.addToBattlefield(player1, new LossarnachCaptain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The tap trigger cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent ownVictim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCaptain();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownVictim.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentVictim.getId());
        harness.passBothPriorities();
        assertThat(ownVictim.isTapped()).isFalse();
        assertThat(opponentVictim.isTapped()).isTrue();
    }

    @Test
    void upkeepTokenEntryTapsOpponentCreature() {
        harness.addToBattlefield(player1, new LossarnachCaptain());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void opponentsUpkeepDoesNotCreateToken() {
        harness.addToBattlefield(player1, new LossarnachCaptain());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentHumanEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new LossarnachCaptain());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new EliteVanguard(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void ownEntryStillTriggersWhenConspiracyReplacesHumanType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCaptain();
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.isTapped()).isTrue();
    }

    private void castCaptain() {
        harness.castFromHand(player1, new LossarnachCaptain(), "{3}{W}");
    }
}
