package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoneRevenantTest extends BaseCardTest {

    private void attackWithRevenant() {
        Permanent revenant = new Permanent(new LoneRevenant());
        revenant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(revenant);
        revenant.setAttacking(true);
    }

    private List<Card> stackTopFour() {
        Card top1 = new GrizzlyBears();
        Card top2 = new Shock();
        Card top3 = new SerraAngel();
        Card top4 = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, top4);
        deck.add(0, top3);
        deck.add(0, top2);
        deck.add(0, top1);
        return List.of(top1, top2, top3, top4);
    }

    @Test
    @DisplayName("Combat damage with no other creatures looks at top four; chosen card to hand, rest on bottom")
    void triggersWhenAlone() {
        List<Card> top = stackTopFour();
        attackWithRevenant();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top.get(2).getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(2));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("No trigger while another creature is controlled")
    void noTriggerWithAnotherCreature() {
        List<Card> top = stackTopFour();
        attackWithRevenant();
        harness.addToBattlefield(player1, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(top);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top.getFirst());
    }
}
