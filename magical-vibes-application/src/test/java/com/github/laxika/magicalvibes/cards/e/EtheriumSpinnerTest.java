package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtheriumSpinner.class, HillGiant.class, GrizzlyBears.class})
class EtheriumSpinnerTest extends BaseCardTest {

    @Test
    void castingSpellWithManaValueFourCreatesFlyingThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    void castingSpellWithManaValueLessThanFourDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EtheriumSpinner());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isZero();
    }
}
