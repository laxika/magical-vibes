package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltBladeProwler.class, Forest.class, WildGuess.class})
class GiltBladeProwlerTest extends BaseCardTest {

    @Test
    void cannotActivateBeforeDiscarding() {
        Permanent prowler = addCreatureReady(player1, new GiltBladeProwler());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void paysLifeAndDrawsAfterDiscardingThisTurn() {
        Permanent prowler = addCreatureReady(player1, new GiltBladeProwler());
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prowler.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void discardCostEnablesActivationBeforeSpellResolves() {
        Permanent prowler = addCreatureReady(player1, new GiltBladeProwler());
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(prowler.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void opponentsDiscardDoesNotEnableActivation() {
        Permanent prowler = addCreatureReady(player1, new GiltBladeProwler());
        harness.setHand(player2, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player2, 0, 1);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void discardFromPreviousTurnDoesNotEnableActivation() {
        Permanent prowler = addCreatureReady(player1, new GiltBladeProwler());
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWithoutManaEvenAfterDiscarding() {
        Permanent prowler = addCreatureReady(player1, new GiltBladeProwler());
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
