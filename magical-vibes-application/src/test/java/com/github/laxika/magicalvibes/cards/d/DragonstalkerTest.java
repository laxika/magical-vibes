package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Dragonstalker.class)
class DragonstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Dragonstalker has protection from Dragons")
    void hasProtectionFromDragons() {
        harness.addToBattlefield(player1, new Dragonstalker());
        Permanent dragonstalker = findPermanent(player1, "Dragonstalker");
        Permanent dragon = new Permanent(createCreature("Test Dragon", CardSubtype.DRAGON));
        Permanent nonDragon = new Permanent(createCreature("Test Angel", CardSubtype.ANGEL));

        assertThat(gqs.hasProtectionFromSource(gd, dragonstalker, dragon)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, dragonstalker, nonDragon)).isFalse();
    }

    private Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
