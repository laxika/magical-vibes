package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexyrIchTekiksHeir.class, LurkerInTheDeep.class, Island.class, GrizzlyBears.class})
class VexyrIchTekiksHeirTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Phyrexian Golem whenever you seek one or more cards")
    void createsGolemWhenYouSeek() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(golem.getCard().getSubtypes())
                .contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Golems you control have vigilance")
    void golemsHaveVigilance() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        Permanent lurker = findPermanent(player1, "Lurker in the Deep");
        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, lurker, Keyword.VIGILANCE)).isFalse();
    }
}
