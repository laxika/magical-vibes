package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartWolf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChiefWargsCompany.class, HeartWolf.class, GrizzlyBears.class})
class ChiefWargsCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 green Wolf token at the beginning of your upkeep")
    void createsWolfTokenAtUpkeep() {
        harness.addToBattlefield(player1, new ChiefWargsCompany());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Wolf"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
    }

    @Test
    @DisplayName("Only creates the Wolf token during its controller's upkeep")
    void doesNotCreateWolfTokenDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ChiefWargsCompany());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Wolf"))
                .count()).isZero();
    }

    @Test
    @DisplayName("Cannot attack without two other Wolves")
    void cannotAttackWithoutTwoOtherWolves() {
        addCreatureReady(player1, new ChiefWargsCompany());
        addCreatureReady(player1, new HeartWolf());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controlling two other Wolves")
    void canAttackWithTwoOtherWolves() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ChiefWargsCompany());
        addCreatureReady(player1, new HeartWolf());
        addCreatureReady(player1, new HeartWolf());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }
}
