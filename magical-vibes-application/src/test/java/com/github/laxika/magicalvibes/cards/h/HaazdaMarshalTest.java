package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
