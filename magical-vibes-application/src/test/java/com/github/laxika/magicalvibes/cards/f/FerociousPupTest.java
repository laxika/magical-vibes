package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FerociousPup.class, Shock.class})
class FerociousPupTest extends BaseCardTest {

    @Test
    @DisplayName("When Ferocious Pup enters, it creates a 2/2 green Wolf token")
    void enteringCreatesWolfToken() {
        harness.setHand(player1, List.of(new FerociousPup()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().getPower()).isEqualTo(2);
        assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
    }

    @Test
    @DisplayName("The Wolf trigger resolves even after the Pup dies")
    void createsWolfAfterPupDiesWithTriggerOnStack() {
        harness.setHand(player1, List.of(new FerociousPup()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ferocious Pup");
        harness.assertNotOnBattlefield(player1, "Wolf");
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Ferocious Pup"));
        harness.assertInGraveyard(player1, "Ferocious Pup");
        harness.assertNotOnBattlefield(player1, "Ferocious Pup");
        harness.assertNotOnBattlefield(player1, "Wolf");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Wolf");
        harness.assertNotOnBattlefield(player2, "Wolf");
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(wolf.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wolf.isTapped()).isFalse();
    }
}
