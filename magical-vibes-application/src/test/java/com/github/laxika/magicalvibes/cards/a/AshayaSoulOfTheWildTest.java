package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshayaSoulOfTheWild.class, Forest.class, GrizzlyBears.class})
class AshayaSoulOfTheWildTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness track the number of lands, including creatures made into lands")
    void powerAndToughnessTrackLands() {
        Permanent ashaya = addCreatureReady(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.isLand(gd, ashaya)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ashaya)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ashaya)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ashaya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ashaya)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nontoken creatures you control become Forest lands and can produce green mana")
    void ownNontokenCreaturesBecomeForestLands() {
        addCreatureReady(player1, new AshayaSoulOfTheWild());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, bears))
                .contains(CardSubtype.FOREST);
        assertThat(gqs.isLand(gd, opponentBears)).isFalse();
        assertThat(gqs.isLand(gd, token)).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Token Creature");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
