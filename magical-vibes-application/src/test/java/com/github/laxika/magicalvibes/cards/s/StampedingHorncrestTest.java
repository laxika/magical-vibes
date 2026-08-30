package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StampedingHorncrestTest extends BaseCardTest {

    @Test
    void hasHasteWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new StampedingHorncrest());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent horncrest = findPermanent(player1, "Stampeding Horncrest");

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotHaveHasteWithoutAnotherDinosaur() {
        harness.addToBattlefield(player1, new StampedingHorncrest());

        Permanent horncrest = findPermanent(player1, "Stampeding Horncrest");

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void nonDinosaurDoesNotGrantHaste() {
        harness.addToBattlefield(player1, new StampedingHorncrest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent horncrest = findPermanent(player1, "Stampeding Horncrest");

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentDinosaurDoesNotGrantHaste() {
        harness.addToBattlefield(player1, new StampedingHorncrest());
        harness.addToBattlefield(player2, createDinosaur());

        Permanent horncrest = findPermanent(player1, "Stampeding Horncrest");

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void losesHasteWhenTheOtherDinosaurLeaves() {
        harness.addToBattlefield(player1, new StampedingHorncrest());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent horncrest = findPermanent(player1, "Stampeding Horncrest");
        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
    }
}
