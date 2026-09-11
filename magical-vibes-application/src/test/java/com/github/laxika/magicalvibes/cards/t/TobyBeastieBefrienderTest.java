package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TobyBeastieBefriender.class, GrizzlyBears.class})
class TobyBeastieBefrienderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 4/4 white Beast token with its restriction")
    void etbCreatesRestrictedBeastToken() {
        Permanent toby = castToby();
        Permanent beast = findPermanent(player1, "Beast");
        beast.setSummoningSick(false);
        toby.setSummoningSick(false);

        assertThat(beast.getCard().isToken()).isTrue();
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(
                        gd.playerBattlefields.get(player1.getId()).indexOf(beast))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Four creature tokens give all creature tokens you control flying")
    void fourCreatureTokensGiveTokensFlying() {
        addTokenCreature("First Token");
        addTokenCreature("Second Token");
        addTokenCreature("Third Token");
        Permanent nontoken = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castToby();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue());
        assertThat(gqs.hasKeyword(gd, nontoken, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Fewer than four creature tokens do not grant flying")
    void fewerThanFourCreatureTokensDoNotGiveFlying() {
        addTokenCreature("First Token");
        addTokenCreature("Second Token");

        castToby();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .noneSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue());
    }

    private Permanent castToby() {
        harness.setHand(player1, List.of(new TobyBeastieBefriender()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Toby, Beastie Befriender");
    }

    private void addTokenCreature(String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, token);
        permanent.setSummoningSick(false);
    }
}
