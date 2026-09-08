package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaptiveAudienceTest extends BaseCardTest {

    private static final String LIFE = "Your life total becomes 4";
    private static final String DISCARD = "Discard your hand";
    private static final String ZOMBIES = "Each opponent creates five 2/2 black Zombie creature tokens";

    @Test
    @DisplayName("Enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.setHand(player1, List.of(new CaptiveAudience()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0);
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
}
