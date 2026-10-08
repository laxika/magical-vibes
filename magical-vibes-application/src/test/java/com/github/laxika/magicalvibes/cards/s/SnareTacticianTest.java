package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.cards.f.FlourishingFox;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnareTactician.class, Censor.class, Compulsion.class, GrizzlyBears.class, FlourishingFox.class, Plains.class})
class SnareTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card taps a target creature an opponent controls")
    void cyclingTapsOpponentsCreature() {
        harness.addToBattlefield(player1, new SnareTactician());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A normal discard does not trigger Snare Tactician")
    void normalDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new SnareTactician());
        harness.addToBattlefield(player1, new Compulsion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The cycling trigger cannot target a creature you control")
    void cyclingTriggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new SnareTactician());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent cycling a card does not trigger Snare Tactician")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SnareTactician());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FlourishingFox());
        harness.setHand(player2, List.of(new FlourishingFox()));
        harness.setLibrary(player2, List.of(new SnareTactician()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(opponentCreature.isTapped()).isFalse();
        harness.assertInHand(player2, "Snare Tactician");
        harness.assertInGraveyard(player2, "Flourishing Fox");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cycling still draws when there is no opposing creature to target")
    void cyclingWithoutLegalTargetStillDraws() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new SnareTactician());
        harness.setHand(player1, List.of(new FlourishingFox()));
        harness.setLibrary(player1, List.of(new SnareTactician()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(tactician.isTapped()).isFalse();
        harness.assertInHand(player1, "Snare Tactician");
        harness.assertInGraveyard(player1, "Flourishing Fox");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The tap trigger resolves before the cycling draw")
    void tapTriggerResolvesBeforeCyclingDraw() {
        harness.addToBattlefield(player1, new SnareTactician());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnareTactician());
        harness.setHand(player1, List.of(new FlourishingFox()));
        harness.setLibrary(player1, List.of(new SnareTactician()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();
        harness.assertInHand(player1, "Snare Tactician");
    }

    @Test
    @DisplayName("An already tapped opposing creature is a legal target")
    void canTargetAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new SnareTactician());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnareTactician());
        target.setTapped(true);
        harness.setHand(player1, List.of(new FlourishingFox()));
        harness.setLibrary(player1, List.of(new SnareTactician()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Snare Tactician");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The cycling trigger cannot target an opposing noncreature permanent")
    void cannotTargetOpposingNoncreature() {
        harness.addToBattlefield(player1, new SnareTactician());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnareTactician());
        harness.setHand(player1, List.of(new FlourishingFox()));
        harness.setLibrary(player1, List.of(new SnareTactician()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(land.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
        harness.assertInHand(player1, "Snare Tactician");
    }
}
