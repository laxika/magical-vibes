package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasOnslaught;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RikuOfManyPaths.class, MishrasOnslaught.class, GrizzlyBears.class})
class RikuOfManyPathsTest extends BaseCardTest {

    private static final String EXILE_MODE =
            "Exile the top card of your library. Until the end of your next turn, you may play it";
    private static final String COUNTER_MODE =
            "Put a +1/+1 counter on Riku of Many Paths. It gains trample until end of turn";
    private static final String TOKEN_MODE = "Create a 1/1 blue Bird creature token with flying";

    @Test
    @DisplayName("Exiles the top card when its mode is chosen")
    void exilesTopCard() {
        addRiku();
        harness.setHand(player1, List.of(new MishrasOnslaught()));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        castModal(EXILE_MODE);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Adds a counter and grants trample until end of turn")
    void counterMode() {
        Permanent riku = addRiku();
        harness.setHand(player1, List.of(new MishrasOnslaught()));

        castModal(COUNTER_MODE);

        assertThat(riku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, riku, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creates a blue Bird token with flying")
    void tokenMode() {
        addRiku();
        harness.setHand(player1, List.of(new MishrasOnslaught()));

        castModal(TOKEN_MODE);

        Permanent bird = findPermanents(player1, "Bird").getFirst();
        assertThat(bird.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addRiku() {
        return addCreatureReady(player1, new RikuOfManyPaths());
    }

    private void castModal(String rikuMode) {
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.handleListChoice(player1, rikuMode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
