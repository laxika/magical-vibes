package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LocthwainLancer.class, BenalishKnight.class, GrizzlyBears.class, Murder.class})
class LocthwainLancerTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken Knight dying makes each opponent lose 1 life and draws a card")
    void nontokenKnightDies() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LocthwainLancer());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());

        killWithMurder(player1, knight);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A non-Knight dying does not trigger Locthwain Lancer")
    void nonKnightDies() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LocthwainLancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithMurder(player1, creature);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A token Knight dying does not trigger Locthwain Lancer")
    void tokenKnightDies() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LocthwainLancer());
        Card tokenKnight = new BenalishKnight();
        tokenKnight.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenKnight);

        killWithMurder(player1, token);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Locthwain Lancer's own nontoken death triggers its ability")
    void ownDeathTriggersAbility() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player2, 20);
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new LocthwainLancer());

        killWithMurder(player1, lancer);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void killWithMurder(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
