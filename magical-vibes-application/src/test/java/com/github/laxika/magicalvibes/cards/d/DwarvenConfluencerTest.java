package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenConfluencer.class, GrizzlyBears.class, ManaConfluence.class, Mountain.class})
class DwarvenConfluencerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroying a nontoken land creates a Mana Confluence token for its controller")
    void destroysNontokenLandAndCreatesManaConfluenceToken() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, battlefieldIndex(player1, confluencer), 0, null, mountain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mana Confluence"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(token.getCard().getActivatedAbilities()).hasSize(1);

        harness.setLife(player2, 20);
        harness.activateAbility(player2, battlefieldIndex(player2, token), 0, null, null);
        harness.handleListChoice(player2, "RED");

        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dwarven Confluencer cannot target a land token")
    void cannotTargetLandToken() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent landToken = harness.addToBattlefieldAndReturn(player2, landToken());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, confluencer), 0, null, landToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dwarven Confluencer cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent confluencer = addCreatureReady(player1, new DwarvenConfluencer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, confluencer), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private static Card landToken() {
        Card card = new Card();
        card.setName("Land Token");
        card.setType(CardType.LAND);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(null);
        card.setColors(java.util.List.of());
        card.setPower(0);
        card.setToughness(0);
        return card;
    }
}
