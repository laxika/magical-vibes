package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XantchaSleeperAgent.class})
class XantchaSleeperAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters under an opponent's control")
    void entersUnderOpponentsControl() {
        XantchaSleeperAgent xantcha = new XantchaSleeperAgent();
        xantcha.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(xantcha));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Xantcha, Sleeper Agent");
        harness.assertOnBattlefield(player2, "Xantcha, Sleeper Agent");
    }

    @Test
    @DisplayName("Cannot attack its owner or its owner's planeswalkers")
    void cannotAttackOwnerOrOwnersPlaneswalkers() {
        XantchaSleeperAgent xantchaCard = new XantchaSleeperAgent();
        xantchaCard.setOwnerId(player1.getId());
        Permanent xantcha = addCreatureReady(player2, xantchaCard);

        Card planeswalker = new Card();
        planeswalker.setName("Test Walker");
        planeswalker.setType(CardType.PLANESWALKER);
        Permanent walker = harness.addToBattlefieldAndReturn(player1, planeswalker);

        assertThat(als.canAttack(gd, xantcha, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, xantcha, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, xantcha, walker.getId())).isFalse();
    }

    @Test
    @DisplayName("Any player may activate it; its controller loses life and the activator draws")
    void anyPlayerMayActivate() {
        XantchaSleeperAgent xantchaCard = new XantchaSleeperAgent();
        xantchaCard.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, xantchaCard);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
