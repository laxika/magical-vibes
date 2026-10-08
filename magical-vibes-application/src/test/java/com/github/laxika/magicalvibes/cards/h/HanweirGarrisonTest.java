package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HanweirGarrison.class, HanweirTheWrithingTownship.class, InvasionOfInnistrad.class})
class HanweirGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two 1/1 Human tokens tapped and attacking")
    void attackCreatesTokensTappedAndAttacking() {
        addCreatureReady(player1, new HanweirGarrison());

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Human"))
                .toList();
        assertThat(tokens).hasSize(2);
        tokens.forEach(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Does not create tokens when not attacking")
    void noTokensWhenNotAttacking() {
        addCreatureReady(player1, new HanweirGarrison());

        declareAttackers(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
    }

    @Test
    @DisplayName("The melded face creates two colorless 3/2 Eldrazi Horror tokens attacking")
    void townshipAttackCreatesTokens() {
        addCreatureReady(player1, new HanweirTheWrithingTownship());
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        tokens.forEach(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELDRAZI, CardSubtype.HORROR);
        });
    }

    @Test
    @DisplayName("Garrison tokens can choose to attack a battle protected by the opponent")
    void tokensCanAttackBattle() {
        addCreatureReady(player1, new HanweirGarrison());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(battle.getId());
        assertThat(choice.validPlayerIds()).contains(player2.getId());
    }
}
