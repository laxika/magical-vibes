package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SomberwaldBeastmaster.class, GrizzlyBears.class})
class SomberwaldBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 2/2 Wolf, a 3/3 Beast, and a 4/4 Beast")
    void createsThreeDifferentTokens() {
        harness.setHand(player1, List.of(new SomberwaldBeastmaster()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Wolf", "Beast", "Beast");
        assertThat(tokens).extracting(permanent -> gqs.getEffectivePower(gd, permanent))
                .containsExactlyInAnyOrder(2, 3, 4);
        assertThat(tokens).extracting(permanent -> gqs.getEffectiveToughness(gd, permanent))
                .containsExactlyInAnyOrder(2, 3, 4);
    }

    @Test
    @DisplayName("Gives deathtouch to creature tokens but not nontoken creatures")
    void creatureTokensHaveDeathtouch() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SomberwaldBeastmaster()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
    }
}
