package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionsOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("A matching creature spell is copied as a token")
    void matchingCreatureSpellIsCopiedAsToken() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(creature("Test Elf", CardSubtype.ELF)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Test Elf"))
                .toList();
        assertThat(elves).hasSize(2);
        assertThat(elves).anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(elves).anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("A non-creature spell of the chosen type also triggers")
    void matchingNonCreatureSpellAlsoTriggers() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(sorcery("Test Tribal Spell", CardSubtype.ELF)));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("A spell of another type is not copied")
    void differentTypeDoesNotTrigger() {
        addReflectionsChoosing(CardSubtype.ELF);
        harness.setHand(player1, List.of(creature("Test Goblin", CardSubtype.GOBLIN)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void addReflectionsChoosing(CardSubtype subtype) {
        Permanent reflections = new Permanent(new ReflectionsOfLittjara());
        reflections.setChosenSubtype(subtype);
        gd.playerBattlefields.get(player1.getId()).add(reflections);
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card sorcery(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
