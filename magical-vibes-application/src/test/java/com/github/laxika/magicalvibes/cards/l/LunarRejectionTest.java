package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LunarRejection.class, Forest.class, GrizzlyBears.class})
class LunarRejectionTest extends BaseCardTest {

    @Test
    void normalCastReturnsWolfOrWerewolfAndDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Wolf", CardSubtype.WOLF));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wolf");
        harness.assertInHand(player2, "Wolf");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void normalCastCannotTargetOtherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Bear", CardSubtype.BEAR));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wolf or Werewolf");
    }

    @Test
    void cleaveCastReturnsAnyCreatureAndDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                creatureWithSubtype("Bear", CardSubtype.BEAR));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bear");
        harness.assertInHand(player2, "Bear");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void cleaveCastCannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LunarRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private static Card creatureWithSubtype(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
