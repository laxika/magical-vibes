package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SultaiEmissary;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FormlessNurturing.class, SultaiEmissary.class})
class FormlessNurturingTest extends BaseCardTest {

    @Test
    void manifestsTopCardAndPutsOnePlusOneCounterOnIt() {
        Card topCard = new SultaiEmissary();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new FormlessNurturing(), "{3}{G}");
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);
    }

    @Test
    void doesNothingWhenItsLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new FormlessNurturing(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void manifestsANoncreatureAndOnlyRemovesTheTopCard() {
        Card topCard = new FormlessNurturing();
        Card nextCard = new SultaiEmissary();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.castFromHand(player1, new FormlessNurturing(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void retainsTheCounterWhenTheManifestedCreatureTurnsFaceUp() {
        harness.setLibrary(player1, List.of(new SultaiEmissary()));
        harness.castFromHand(player1, new FormlessNurturing(), "{3}{G}");
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
    }

}
