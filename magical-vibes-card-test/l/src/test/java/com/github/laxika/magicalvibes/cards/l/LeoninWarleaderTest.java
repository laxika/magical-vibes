package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninWarleader.class})
class LeoninWarleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two 1/1 white Cat tokens with lifelink, tapped and attacking")
    void attackCreatesLifelinkCatTokens() {
        addCreatureReady(player1, new LeoninWarleader());

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.passBothPriorities());

        List<Permanent> cats = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .toList();
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
    }

    @Test
    @DisplayName("No tokens are created when Leonin Warleader does not attack")
    void noTriggerWithoutAttacking() {
        addCreatureReady(player1, new LeoninWarleader());

        declareAttackers(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("The attacking Cat tokens deal combat damage and gain life immediately")
    void tokensDealCombatDamageWithLifelink() {
        addCreatureReady(player1, new LeoninWarleader());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.passBothPriorities());
        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Removing Warleader does not stop its attack trigger creating attacking tokens")
    void tokensAreCreatedAfterSourceLeaves() {
        Permanent warleader = addCreatureReady(player1, new LeoninWarleader());
        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(warleader);
        gd.playerGraveyards.get(player1.getId()).add(warleader.getCard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.passBothPriorities());

        assertThat(findPermanents(player1, "Cat")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(gd.stack).isEmpty();
    }

}
