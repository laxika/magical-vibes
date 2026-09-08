package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JolraelMwonvuliRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates one 2/2 green Cat")
    void secondDrawCreatesCatOnlyOnce() {
        harness.addToBattlefield(player1, new JolraelMwonvuliRecluse());
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(1);
        assertThat(cats.get(0).getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(cats.get(0).getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
        assertThat(gqs.getEffectivePower(gd, cats.get(0))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cats.get(0))).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability sets your creatures to your hand size")
    void activatedAbilitySetsOwnCreaturesToHandSize() {
        Permanent jolrael = harness.addToBattlefieldAndReturn(player1, new JolraelMwonvuliRecluse());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
