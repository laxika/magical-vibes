package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodmateDragon.class})
class BroodmateDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 4/4 red Dragon token with flying")
    void etbCreatesDragonToken() {
        harness.castFromHand(player1, new BroodmateDragon(), "{3}{B}{R}{G}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = dragonTokens(player1);
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast creates a token for the entering creature's controller")
    void noncastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new BroodmateDragon());

        assertThat(dragonTokens(player2)).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(dragonTokens(player1)).isEmpty();
        assertThat(dragonTokens(player2)).hasSize(1);
        Permanent token = dragonTokens(player2).getFirst();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Broodmate Dragon");
    }
    private List<Permanent> dragonTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .toList();
    }
}
