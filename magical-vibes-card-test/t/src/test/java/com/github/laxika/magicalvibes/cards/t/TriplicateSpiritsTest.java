package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SelflessCathar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriplicateSpirits.class, SelflessCathar.class})
class TriplicateSpiritsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Spirit tokens with flying")
    void createsThreeFlyingSpiritTokens() {
        harness.setHand(player1, List.of(new TriplicateSpirits()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(3);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getEffectivePower()).isEqualTo(1);
            assertThat(spirit.getEffectiveToughness()).isEqualTo(1);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Newly entered white creatures can convoke the white mana requirements")
    void newlyEnteredCreaturesPayWhiteMana() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
        harness.setHand(player1, List.of(new TriplicateSpirits()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Convoke can pay the entire cost without mana")
    void convokePaysEntireCost() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new SelflessCathar()))
                .toList();
        harness.setHand(player1, List.of(new TriplicateSpirits()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }
}
