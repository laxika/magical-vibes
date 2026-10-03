package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonTrainer.class})
class DragonTrainerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 4/4 red Dragon token with flying")
    void etbCreatesDragonToken() {
        harness.castFromHand(player1, new DragonTrainer(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = dragonTokens(player1);
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Dragon is created when the ETB trigger resolves, not when the spell resolves")
    void tokenWaitsForTriggerResolution() {
        harness.castFromHand(player1, new DragonTrainer(), "{3}{R}{R}");
        assertThat(dragonTokens(player1)).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon Trainer");
        assertThat(dragonTokens(player1)).isEmpty();

        harness.passBothPriorities();

        assertThat(dragonTokens(player1)).hasSize(1);
        assertThat(dragonTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast creates a Dragon for the entering creature's controller")
    void noncastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new DragonTrainer());
        assertThat(dragonTokens(player2)).isEmpty();

        harness.passBothPriorities();

        assertThat(dragonTokens(player2)).hasSize(1);
        assertThat(dragonTokens(player1)).isEmpty();
        assertThat(dragonTokens(player2).getFirst().isTapped()).isFalse();
    }

    private List<Permanent> dragonTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .toList();
    }
}
