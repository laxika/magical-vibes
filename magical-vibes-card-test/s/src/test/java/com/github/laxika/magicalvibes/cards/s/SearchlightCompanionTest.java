package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchlightCompanion.class})
class SearchlightCompanionTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesColorlessSpiritToken() {
        harness.setHand(player1, List.of(new SearchlightCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Spirit");
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColors()).isEmpty();
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().hasType(CardType.ARTIFACT)).isFalse();
                });
    }

    @Test
    void spiritIsCreatedOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new SearchlightCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Searchlight Companion");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCreatesSpiritUnderTheirControl() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SearchlightCompanion()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(findPermanent(player2, "Spirit").isTapped()).isFalse();
        assertThat(findPermanent(player2, "Spirit").getCard().getKeywords()).isEmpty();
    }
}
