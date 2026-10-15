package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PriceOfFame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaazdaMarshal.class, GrizzlyBears.class})
class HaazdaMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a lifelink Soldier token when it attacks with two other creatures")
    void createsLifelinkSoldierTokenWithTwoOtherAttackers() {
        addReady(new HaazdaMarshal());
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Does not create a token when fewer than three creatures attack")
    void doesNotCreateTokenWithFewerThanTwoOtherAttackers() {
        addReady(new HaazdaMarshal());
        addReady(new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not create a token when Haazda Marshal does not attack")
    void doesNotCreateTokenWhenMarshalStaysBack() {
        addReady(new HaazdaMarshal());
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());
        addReady(new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addReady(Card card) {
        return addCreatureReady(player1, card);
    }

    @Test
    @DisplayName("Each attacking Marshal creates one token even with more than three attackers")
    void eachMarshalTriggersOnceWithFourAttackers() {
        for (int i = 0; i < 4; i++) {
            addReady(new HaazdaMarshal());
        }

        declareAttackers(List.of(0, 1, 2, 3));
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(4)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
                    assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
                    assertThat(token.isAttacking()).isFalse();
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @CardUsed({PriceOfFame.class})
    @DisplayName("Attack triggers still create tokens after an attacking Marshal is destroyed")
    void triggersResolveAfterAttackerAndSourceLeaveBattlefield() {
        Permanent marshal = addReady(new HaazdaMarshal());
        addReady(new HaazdaMarshal());
        addReady(new HaazdaMarshal());
        harness.setHand(player2, List.of(new PriceOfFame()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0, 1, 2));
        harness.castInstant(player2, 0, marshal.getId());
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(marshal.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(3);
    }
}
