package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinotaurRoxxonCEO.class, GrizzlyBears.class, DoomBlade.class, Murder.class})
class MinotaurRoxxonCEOTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature dying creates a 2/1 black Villain with menace")
    void anotherNontokenCreatureDeathCreatesVillain() {
        harness.addToBattlefield(player1, new MinotaurRoxxonCEO());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithDoomBlade(bears);

        Permanent villain = findPermanent(player1, "Villain");
        assertThat(villain.getCard().isToken()).isTrue();
        assertThat(villain.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(villain.getCard().getSubtypes()).contains(CardSubtype.VILLAIN);
        assertThat(villain.getCard().getKeywords()).contains(Keyword.MENACE);
        assertThat(villain.getEffectivePower()).isEqualTo(2);
        assertThat(villain.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Roxxon dying creates a Villain")
    void selfDeathCreatesVillain() {
        Permanent roxxon = harness.addToBattlefieldAndReturn(player1, new MinotaurRoxxonCEO());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, roxxon.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Villain")).hasSize(1);
    }

    @Test
    @DisplayName("A token creature dying does not trigger Roxxon")
    void tokenCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new MinotaurRoxxonCEO());
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());

        killWithDoomBlade(token);

        assertThat(findPermanents(player1, "Villain")).isEmpty();
    }

    private void killWithDoomBlade(Permanent target) {
        harness.setHand(player1, java.util.List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }
}
