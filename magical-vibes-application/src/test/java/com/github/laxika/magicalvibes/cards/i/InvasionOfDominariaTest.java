package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraFaithkeeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, InvasionOfDominaria.class, SerraFaithkeeper.class})
class InvasionOfDominariaTest extends BaseCardTest {

    @Test
    @DisplayName("When Invasion of Dominaria enters, you gain 4 life and draw a card")
    void entersGainsLifeAndDrawsCard() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        int lifeBefore = gd.getLife(player1.getId());

        castInvasion();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Defeat exiles the Siege and casts Serra Faithkeeper transformed")
    void defeatCastsBackFace() {
        castInvasion();

        Permanent battle = findPermanent("Invasion of Dominaria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent faithkeeper = findPermanent("Serra Faithkeeper");
        assertThat(faithkeeper.isTransformed()).isTrue();
    }

    private void castInvasion() {
        Card invasion = new InvasionOfDominaria();
        harness.setHand(player1, List.of(invasion));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
