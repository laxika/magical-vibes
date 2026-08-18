package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EndlessSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 1/1 green Snake for each card in hand and applies Epic")
    void createsSnakesForCardsInHandAndAppliesEpic() {
        harness.setHand(player1, List.of(new EndlessSwarm(), new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = snakeTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(token -> token.getEffectivePower() == 1
                && token.getEffectiveToughness() == 1
                && token.getCard().getColor() == CardColor.GREEN
                && token.getCard().getSubtypes().contains(CardSubtype.SNAKE));
        assertThat(gd.playersCantCastSpellsForRestOfGame).contains(player1.getId());
    }

    @Test
    @DisplayName("Copies the token creation at the beginning of the controller's upkeep")
    void copiesTokenCreationAtUpkeep() {
        harness.setHand(player1, List.of(new EndlessSwarm(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(snakeTokens()).hasSize(1);

        harness.setHand(player1, List.of(new Shock(), new Forest()));
        harness.setLibrary(player1, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(snakeTokens()).hasSize(3);
    }

    private List<Permanent> snakeTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SNAKE))
                .toList();
    }
}
