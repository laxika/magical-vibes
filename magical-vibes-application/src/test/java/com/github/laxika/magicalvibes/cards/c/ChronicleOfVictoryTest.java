package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChronicleOfVictoryTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, int power, int toughness,
                                       CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card createTribalInstant(String name, String manaCost, CardColor color,
                                            CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private Permanent addChronicle(CardSubtype chosenSubtype) {
        Permanent chronicle = new Permanent(new ChronicleOfVictory());
        chronicle.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player1.getId()).add(chronicle);
        return chronicle;
    }

    @Test
    void resolvingChroniclePromptsForCreatureType() {
        harness.setHand(player1, List.of(new ChronicleOfVictory()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    void chosenTypeCreaturesGetBoostAndKeywords() {
        harness.addToBattlefield(player1,
                createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF));
        addChronicle(CardSubtype.ELF);

        Permanent elf = findPermanent(player1, "Llanowar Elves");

        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void differentTypeCreaturesDoNotGetBoostOrKeywords() {
        harness.addToBattlefield(player1,
                createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN));
        addChronicle(CardSubtype.ELF);

        Permanent goblin = findPermanent(player1, "Goblin Piker");

        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, goblin).toughness()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void castingChosenTypeNoncreatureSpellDrawsACard() {
        addChronicle(CardSubtype.GOBLIN);
        Card tribalInstant = createTribalInstant("Tarfire", "{R}", CardColor.RED, CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(tribalInstant));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void castingDifferentTypeSpellDoesNotDraw() {
        addChronicle(CardSubtype.ELF);
        Card goblin = createCreature("Goblin Piker", "{R}", 1, 1, CardColor.RED, CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
