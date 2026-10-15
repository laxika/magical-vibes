package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LinvalaThePreserver.class, CanopyGorger.class})
class LinvalaThePreserverTest extends BaseCardTest {

    private long angelTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ANGEL))
                .count();
    }

    private void castLinvala() {
        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Behind on life and creatures: gains 5 life and creates an Angel")
    void bothClausesApply() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());
        harness.addToBattlefield(player2, new CanopyGorger());

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
        harness.addToBattlefield(player2, new CanopyGorger());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Each clause resolves independently")
    void clausesResolveIndependently() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Creature clause applies without the life clause")
    void creatureClauseAppliesWithoutLifeClause() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player2, new CanopyGorger());
        harness.addToBattlefield(player2, new CanopyGorger());

        castLinvala();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(angelTokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Neither ability triggers when both conditions are false on entry")
    void falseEntryConditionsDoNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());

        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Falling behind on life after entry cannot create a life-gain trigger")
    void lifeConditionMustBeTrueOnEntry() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());
        harness.addToBattlefield(player2, new CanopyGorger());

        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(angelTokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Falling behind on creatures after entry cannot create an Angel trigger")
    void creatureConditionMustBeTrueOnEntry() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());

        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new CanopyGorger());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Life condition is checked again when the ability resolves")
    void lifeConditionMustStillBeTrueOnResolution() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Creature condition is checked again when the ability resolves")
    void creatureConditionMustStillBeTrueOnResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());
        harness.addToBattlefield(player2, new CanopyGorger());

        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new CanopyGorger());
        harness.passBothPriorities();

        assertThat(angelTokenCount()).isZero();
    }

    @Test
    @DisplayName("Both abilities trigger separately and resolve with priority between them")
    void bothAbilitiesUseSeparateStackEntries() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CanopyGorger());
        harness.addToBattlefield(player2, new CanopyGorger());

        harness.castFromHand(player1, new LinvalaThePreserver(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        boolean lifeResolvedFirst = gd.getLife(player1.getId()) == 15;
        if (lifeResolvedFirst) {
            assertThat(angelTokenCount()).isZero();
        } else {
            harness.assertLife(player1, 10);
            assertThat(angelTokenCount()).isEqualTo(1);
        }

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(angelTokenCount()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
