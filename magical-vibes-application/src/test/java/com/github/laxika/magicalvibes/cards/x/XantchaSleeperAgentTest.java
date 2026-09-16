package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XantchaSleeperAgent.class, Forest.class})
class XantchaSleeperAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Xantcha enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.setHand(player1, List.of(new XantchaSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Xantcha, Sleeper Agent");
        harness.assertOnBattlefield(player2, "Xantcha, Sleeper Agent");
    }

    @Test
    @DisplayName("Xantcha can't attack its owner or that owner's planeswalkers")
    void cannotAttackOwnerOrOwnersPlaneswalker() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        Permanent xantcha = addCreatureReady(player2, card);

        assertThat(als.canAttackDefender(gd, xantcha, player1.getId())).isFalse();

        Card planeswalkerCard = new Card();
        planeswalkerCard.setType(CardType.PLANESWALKER);
        planeswalkerCard.setOwnerId(player1.getId());
        Permanent planeswalker = new Permanent(planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);

        assertThat(als.canAttackDefender(gd, xantcha, planeswalker.getId())).isFalse();
    }

    @Test
    @DisplayName("Any player may activate Xantcha's ability, making its controller lose life while the activator draws")
    void anyPlayerMayActivate() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        harness.addToBattlefield(player1, card);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
