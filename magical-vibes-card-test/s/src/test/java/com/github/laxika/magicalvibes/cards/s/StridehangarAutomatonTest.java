package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EfficientConstruction;
import com.github.laxika.magicalvibes.cards.h.HangedExecutioner;
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

@CardUsed({StridehangarAutomaton.class, EfficientConstruction.class, Spellbook.class, HangedExecutioner.class})
class StridehangarAutomatonTest extends BaseCardTest {

    @Test
    void addsAndBoostsThopterWhenAnArtifactTokenIsCreated() {
        harness.addToBattlefield(player1, new StridehangarAutomaton());
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
            assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void doesNotAddThopterWhenANonartifactTokenIsCreated() {
        harness.addToBattlefield(player1, new StridehangarAutomaton());
        harness.setHand(player1, List.of(new HangedExecutioner()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }
}
