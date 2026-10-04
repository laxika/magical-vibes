package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HareApparent.class, BurstLightning.class})
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

        harness.castFromHand(player1, new HareApparent(), "{1}{W}");
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

        harness.castFromHand(player1, new HareApparent(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rabbitTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Removing the entering Hare does not stop its trigger from counting other Hares")
    void triggerStillCreatesTokensAfterSourceDies() {
        addCreatureReady(player1, new HareApparent());
        harness.castFromHand(player1, new HareApparent(), "{1}{W}");
        harness.passBothPriorities();

        Permanent enteringHare = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, enteringHare.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enteringHare);
        harness.assertInGraveyard(player1, "Hare Apparent");
        harness.passBothPriorities();

        assertThat(rabbitTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The trigger counts other Hares at resolution rather than when it triggers")
    void otherHareRemovedInResponseIsNotCounted() {
        addCreatureReady(player1, new HareApparent());
        Permanent otherHare = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castFromHand(player1, new HareApparent(), "{1}{W}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, otherHare.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherHare);
        harness.passBothPriorities();

        assertThat(rabbitTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Previously created Rabbits are not counted as Hare Apparents")
    void rabbitTokensDoNotIncreaseSubsequentTokenCounts() {
        addCreatureReady(player1, new HareApparent());
        harness.castFromHand(player1, new HareApparent(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(rabbitTokens(player1)).hasSize(1);

        harness.castFromHand(player1, new HareApparent(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rabbitTokens(player1)).hasSize(3);
    }
}
