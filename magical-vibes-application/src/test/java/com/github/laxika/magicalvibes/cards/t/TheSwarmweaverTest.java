package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSwarmweaver.class, Divination.class, Forest.class, GiantSpider.class,
        GrizzlyBears.class, Shock.class})
class TheSwarmweaverTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two 1/1 black and green Insect tokens with flying")
    void entersWithInsectTokens() {
        castSwarmweaver(List.of(new GrizzlyBears(), new Forest(), new Shock()));

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("With delirium, buffs Insects and Spiders with +1/+1 and deathtouch")
    void deliriumBuffsInsectsAndSpiders() {
        harness.setGraveyard(player1, fourCardTypes());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int spiderPower = gqs.getEffectivePower(gd, spider);
        int spiderToughness = gqs.getEffectiveToughness(gd, spider);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int bearToughness = gqs.getEffectiveToughness(gd, bear);

        castSwarmweaver(fourCardTypes());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(spiderPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(spiderToughness + 1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(bearToughness);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue();
        });
    }

    @Test
    @DisplayName("Loses the bonus when delirium is lost")
    void losesBonusWhenDeliriumIsLost() {
        harness.setGraveyard(player1, fourCardTypes());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.addToBattlefield(player1, new TheSwarmweaver());

        int buffedPower = gqs.getEffectivePower(gd, spider);
        int buffedToughness = gqs.getEffectiveToughness(gd, spider);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.DEATHTOUCH)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(buffedPower - 1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(buffedToughness - 1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.DEATHTOUCH)).isFalse();
    }

    private void castSwarmweaver(List<com.github.laxika.magicalvibes.model.Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TheSwarmweaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<com.github.laxika.magicalvibes.model.Card> fourCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Divination());
    }
}
