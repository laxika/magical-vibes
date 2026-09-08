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

class SunCrestedPterodonTest extends BaseCardTest {

    @Test
    void hasVigilanceWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent pterodon = findPermanent(player1, "Sun-Crested Pterodon");

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotHaveVigilanceWithoutAnotherDinosaur() {
        harness.addToBattlefield(player1, new SunCrestedPterodon());

        Permanent pterodon = findPermanent(player1, "Sun-Crested Pterodon");

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void nonDinosaurDoesNotGrantVigilance() {
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent pterodon = findPermanent(player1, "Sun-Crested Pterodon");

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void opponentDinosaurDoesNotGrantVigilance() {
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player2, createDinosaur());

        Permanent pterodon = findPermanent(player1, "Sun-Crested Pterodon");

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void losesVigilanceWhenTheOtherDinosaurLeaves() {
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent pterodon = findPermanent(player1, "Sun-Crested Pterodon");
        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.hasKeyword(gd, pterodon, Keyword.VIGILANCE)).isFalse();
    }

    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
    }
}
