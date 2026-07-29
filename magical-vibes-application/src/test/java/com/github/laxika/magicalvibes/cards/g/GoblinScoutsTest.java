package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinScoutsTest extends BaseCardTest {

    private List<Permanent> scoutTokens(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goblin Scout"))
                .toList();
    }

    private void cast() {
        harness.setHand(player1, List.of(new GoblinScouts()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving creates three 1/1 red Goblin Scout tokens with mountainwalk")
    void resolvingCreatesThreeTokens() {
        cast();

        List<Permanent> tokens = scoutTokens(player1.getId());
        assertThat(tokens).hasSize(3);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.SCOUT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.MOUNTAINWALK);
        }
    }

    @Test
    @DisplayName("Tokens enter only under the caster's control")
    void tokensEnterUnderControllerControl() {
        cast();

        assertThat(scoutTokens(player1.getId())).hasSize(3);
        assertThat(scoutTokens(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Goblin Scouts goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        cast();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Scouts");
    }
}
