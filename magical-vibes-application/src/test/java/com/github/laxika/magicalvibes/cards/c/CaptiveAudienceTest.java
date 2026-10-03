package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptiveAudience.class})
class CaptiveAudienceTest extends BaseCardTest {

    private static final String LIFE = "Your life total becomes 4";
    private static final String DISCARD = "Discard your hand";
    private static final String ZOMBIES = "Each opponent creates five 2/2 black Zombie creature tokens";

    @Test
    @DisplayName("Enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.castFromHand(player1, new CaptiveAudience(), "{5}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Captive Audience");
        harness.assertOnBattlefield(player2, "Captive Audience");
    }

    @Test
    @DisplayName("Life-total mode sets its controller's life to 4")
    void lifeTotalMode() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, LIFE);
        harness.passBothPriorities();

        harness.assertLife(player2, 4);
    }

    @Test
    @DisplayName("Discard mode discards its controller's entire hand")
    void discardMode() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        harness.setHand(player2, List.of(new CaptiveAudience(), new CaptiveAudience()));

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, DISCARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Zombie mode gives each opponent five black Zombies")
    void zombieMode() {
        harness.addToBattlefield(player2, new CaptiveAudience());

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, ZOMBIES);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(5);
        assertThat(findPermanents(player1, "Zombie"))
                .allMatch(zombie -> zombie.getCard().getColor() == CardColor.BLACK
                        && zombie.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("A resolved mode cannot be chosen again")
    void modeIsConsumed() {
        harness.addToBattlefield(player2, new CaptiveAudience());

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, LIFE);
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handleListChoice(player2, LIFE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Enters directly under an opponent's control without a control-change trigger")
    void entersDirectlyUnderOpponentsControl() {
        harness.castFromHand(player1, new CaptiveAudience(), "{5}{B}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Captive Audience");
        harness.assertOnBattlefield(player2, "Captive Audience");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life-total mode can increase its controller's life to 4")
    void lifeTotalModeCanGainLife() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        harness.setLife(player2, 2);

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, LIFE);
        harness.passBothPriorities();

        harness.assertLife(player2, 4);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Life-total mode is legal and consumed when life is already 4")
    void lifeTotalModeAtFourIsConsumed() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        harness.setLife(player2, 4);

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, LIFE);
        harness.passBothPriorities();
        harness.assertLife(player2, 4);

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handleListChoice(player2, LIFE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Discard mode is legal and consumed with an empty hand")
    void discardEmptyHandIsConsumed() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, DISCARD);
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handleListChoice(player2, DISCARD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("No further mode resolves after all three have been chosen")
    void allModesExhausted() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        harness.setHand(player2, List.of());

        for (String mode : List.of(LIFE, DISCARD, ZOMBIES)) {
            advanceToUpkeep(player2);
            harness.handleListChoice(player2, mode);
            harness.passBothPriorities();
        }

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 4);
        assertThat(findPermanents(player1, "Zombie")).hasSize(5);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
        harness.assertOnBattlefield(player2, "Captive Audience");
    }

    @Test
    @DisplayName("The opponent's upkeep does not trigger Captive Audience")
    void onlyControllersUpkeepTriggers() {
        harness.addToBattlefield(player2, new CaptiveAudience());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discard mode puts the controller's cards in their graveyard and preserves the opponent's hand")
    void discardGoesToControllersGraveyard() {
        harness.addToBattlefield(player2, new CaptiveAudience());
        CaptiveAudience first = new CaptiveAudience();
        CaptiveAudience second = new CaptiveAudience();
        CaptiveAudience opponentsCard = new CaptiveAudience();
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(opponentsCard));

        advanceToUpkeep(player2);
        harness.handleListChoice(player2, DISCARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentsCard);
    }
}
