package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThunderOfHooves.class)
class ThunderOfHoovesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of Beasts on the battlefield to nonflying creatures and each player")
    void dealsDamageBasedOnAllBeasts() {
        Permanent ownBeast = harness.addToBattlefieldAndReturn(player1,
                makeCreature("Own Beast", 5, List.of(CardSubtype.BEAST), Set.of()));
        Permanent opponentBeast = harness.addToBattlefieldAndReturn(player2,
                makeCreature("Opponent Beast", 5, List.of(CardSubtype.BEAST), Set.of()));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1,
                makeCreature("Own Creature", 5, List.of(), Set.of()));
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2,
                makeCreature("Flying Creature", 5, List.of(), Set.of(Keyword.FLYING)));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        assertThat(ownBeast.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentBeast.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Uses the Beast count when the spell resolves")
    void countsBeastsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                makeCreature("Target", 5, List.of(), Set.of()));
        harness.setHand(player1, List.of(new ThunderOfHooves()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player2, makeCreature("Beast", 5, List.of(CardSubtype.BEAST), Set.of()));
        harness.addToBattlefield(player1, makeCreature("Another Beast", 5, List.of(CardSubtype.BEAST), Set.of()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals no damage when there are no Beasts")
    void dealsNoDamageWithoutBeasts() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1,
                makeCreature("Own Creature", 5, List.of(), Set.of()));
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2,
                makeCreature("Opponent Creature", 5, List.of(), Set.of()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void cast() {
        harness.setHand(player1, List.of(new ThunderOfHooves()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Card makeCreature(String name, int toughness, List<CardSubtype> subtypes, Set<Keyword> keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(toughness);
        card.setSubtypes(subtypes);
        card.setKeywords(keywords);
        return card;
    }
}
