package com.github.laxika.magicalvibes.cards.h;

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

class HareApparentTest extends BaseCardTest {

    private List<Permanent> rabbitTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Rabbit"))
                .toList();
    }

    @Test
    @DisplayName("ETB creates one Rabbit for each other Hare Apparent you control")
    void etbCreatesRabbitForEachOtherHare() {
        addCreatureReady(player1, new HareApparent());
        addCreatureReady(player1, new HareApparent());
        addCreatureReady(player2, new HareApparent());

        harness.setHand(player1, List.of(new HareApparent()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rabbitTokens(player1)).hasSize(2);
        Permanent token = rabbitTokens(player1).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.RABBIT);
    }

    @Test
    @DisplayName("ETB creates no Rabbit when there are no other Hare Apparents you control")
    void etbExcludesTheEnteringHareAndOpponentsCopies() {
        addCreatureReady(player2, new HareApparent());

        harness.setHand(player1, List.of(new HareApparent()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rabbitTokens(player1)).isEmpty();
    }
}
