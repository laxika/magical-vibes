package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceInvocation.class, FrontierMastodon.class})
class FierceInvocationTest extends BaseCardTest {

    @Test
    void manifestsTopCardAndPutsTwoPlusOnePlusOneCountersOnIt() {
        Card topCard = new FrontierMastodon();
        harness.setHand(player1, List.of(new FierceInvocation()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
    }

    @Test
    void countersRemainWhenManifestedCreatureTurnsFaceUp() {
        Card topCard = new FrontierMastodon();
        harness.setHand(player1, List.of(new FierceInvocation()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestsNoncreatureButCannotTurnItFaceUpForItsManaCost() {
        Card topCard = new FierceInvocation();
        Card nextCard = new FrontierMastodon();
        harness.setHand(player1, List.of(new FierceInvocation()));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNothingWhenItsLibraryIsEmpty() {
        harness.setHand(player1, List.of(new FierceInvocation()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }
}
