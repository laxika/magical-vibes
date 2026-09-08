package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BannerOfKinshipTest extends BaseCardTest {

    private static Card createCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    @Test
    @DisplayName("Choosing a type gives Banner one fellowship counter per matching creature")
    void entersWithFellowshipCountersForChosenType() {
        harness.addToBattlefield(player1, createCreature("Elvish Mystic", CardSubtype.ELF));
        harness.addToBattlefield(player1, createCreature("Llanowar Elves", CardSubtype.ELF));
        harness.addToBattlefield(player1, createCreature("Goblin Piker", CardSubtype.GOBLIN));

        harness.setHand(player1, List.of(new BannerOfKinship()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent banner = findPermanent(player1, "Banner of Kinship");
        assertThat(banner.getCounterCount(CounterType.FELLOWSHIP)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen-type creatures get +1/+1 for each fellowship counter")
    void boostsChosenTypePerFellowshipCounter() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, createCreature("Elvish Mystic", CardSubtype.ELF));
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, createCreature("Goblin Piker", CardSubtype.GOBLIN));
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new BannerOfKinship());
        banner.setChosenSubtype(CardSubtype.ELF);
        banner.setCounterCount(CounterType.FELLOWSHIP, 3);

        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, goblin).toughness()).isEqualTo(0);
    }
}
