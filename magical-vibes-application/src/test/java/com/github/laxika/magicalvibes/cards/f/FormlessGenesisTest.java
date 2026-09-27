package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FormlessGenesis.class, Forest.class, GrizzlyBears.class})
class FormlessGenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Shapeshifter sized by land cards in the graveyard")
    void createsShapeshifterSizedByLandCardsInGraveyard() {
        harness.setHand(player1, List.of(new FormlessGenesis()));
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = shapeshifterToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Retrace discards a land and counts it in the created token's size")
    void retraceCountsDiscardedLand() {
        harness.setGraveyard(player1, List.of(new FormlessGenesis(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = shapeshifterToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Formless Genesis");
    }

    private Permanent shapeshifterToken() {
        return findPermanents(player1, "Shapeshifter").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
