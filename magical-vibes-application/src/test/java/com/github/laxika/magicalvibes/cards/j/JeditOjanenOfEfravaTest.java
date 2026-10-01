package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Jedit Ojanen of Efrava")
@CardUsed({JeditOjanenOfEfrava.class, BloodKnight.class, Forest.class})
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
        Permanent attacker = addCreatureReady(player1, new BloodKnight());
        Permanent blocker = addCreatureReady(player2, new JeditOjanenOfEfrava());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveAllTriggers();

        assertCatWarriorToken(player2);
    }

    @Test
    @DisplayName("Forestwalk prevents blocking Jedit when the defending player controls a Forest")
    void forestwalkPreventsBlockingWithForest() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new BloodKnight());
        Permanent attacker = addCreatureReady(player1, new JeditOjanenOfEfrava());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        resolveAllTriggers();
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
