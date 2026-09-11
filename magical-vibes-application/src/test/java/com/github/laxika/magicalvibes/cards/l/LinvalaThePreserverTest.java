package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LinvalaThePreserver.class, GrizzlyBears.class})
class LinvalaThePreserverTest extends BaseCardTest {

    private long angelTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ANGEL))
                .count();
    }

    private void castLinvala() {
        harness.setHand(player1, List.of(new LinvalaThePreserver()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Behind on life and creatures: gains 5 life and creates an Angel")
    void bothClausesApply() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(angelTokenCount()).isEqualTo(1);

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ANGEL))
                .findFirst()
                .orElseThrow();
        assertThat(angel.getCard().getPower()).isEqualTo(3);
        assertThat(angel.getCard().getToughness()).isEqualTo(3);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equal on life and creatures: neither clause applies")
    void neitherClauseApplies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Each clause resolves independently")
    void clausesResolveIndependently() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Creature clause applies without the life clause")
    void creatureClauseAppliesWithoutLifeClause() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(angelTokenCount()).isEqualTo(1);
    }
}
