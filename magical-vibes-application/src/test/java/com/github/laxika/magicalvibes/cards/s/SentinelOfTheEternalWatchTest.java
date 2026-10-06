package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.u.UnholyHunger;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SentinelOfTheEternalWatch.class, TimberpackWolf.class, UnholyHunger.class})
class SentinelOfTheEternalWatchTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Taps a creature the active opponent controls at the beginning of their combat")
    void tapsTargetOnOpponentsTurn() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());

        advanceToCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger on its controller's own turn")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(wolf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the active opponent's creatures are legal targets")
    void doesNotTriggerWithoutOpponentCreature() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());

        advanceToCombat(player2);

        assertThat(wolf.isTapped()).isFalse();
    }

    @Test
    void excludesControllersCreaturesFromTargetChoices() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        harness.addToBattlefield(player1, new TimberpackWolf());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());

        advanceToCombat(player2);

        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactly(wolf.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    void canTargetAnAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        wolf.setTapped(true);

        advanceToCombat(player2);

        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(wolf.getId());
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    void triggerResolvesAfterSentinelIsDestroyed() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SentinelOfTheEternalWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        harness.setHand(player2, List.of(new UnholyHunger()));

        advanceToCombat(player2);
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, sentinel.getId());
        harness.assertInGraveyard(player1, "Sentinel of the Eternal Watch");
        assertThat(wolf.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    void doesNotRetargetWhenChosenCreatureIsDestroyed() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        harness.setHand(player2, List.of(new UnholyHunger()));

        advanceToCombat(player2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Timberpack Wolf");
        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
