package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Jedit Ojanen of Efrava")
@CardUsed({JeditOjanenOfEfrava.class, GrizzlyBears.class})
class JeditOjanenOfEfravaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 2/2 green Cat Warrior token with forestwalk")
    void attackingCreatesCatWarriorToken() {
        addCreatureReady(player1, new JeditOjanenOfEfrava());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertCatWarriorToken(player1);
    }

    @Test
    @DisplayName("Blocking creates a 2/2 green Cat Warrior token with forestwalk")
    void blockingCreatesCatWarriorToken() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new JeditOjanenOfEfrava());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertCatWarriorToken(player2);
    }

    private void assertCatWarriorToken(com.github.laxika.magicalvibes.model.Player player) {
        List<Permanent> tokens = findPermanents(player, "Cat Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAT, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FORESTWALK);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
}
