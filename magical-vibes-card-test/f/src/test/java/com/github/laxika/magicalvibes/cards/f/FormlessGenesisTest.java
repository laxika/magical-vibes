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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FormlessGenesis.class, Forest.class, GrizzlyBears.class})
class FormlessGenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Shapeshifter sized by land cards in the graveyard")
    void createsShapeshifterSizedByLandCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new GrizzlyBears()));
        harness.castFromHand(player1, new FormlessGenesis(), "{2}{G}");
        harness.passBothPriorities();

        Permanent token = shapeshifterToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).isEmpty();
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(1);
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

    @Test
    @DisplayName("Counts only the controller's lands at resolution and fixes token size")
    void countsLandsAtResolutionAndFixesSize() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new FormlessGenesis(), "{2}{G}");
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities();

        Permanent token = shapeshifterToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(findPermanents(player2, "Shapeshifter")).isEmpty();
    }

    @Test
    @DisplayName("With no land cards the zero-toughness token does not survive")
    void zeroToughnessTokenDies() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new FormlessGenesis(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shapeshifter");
        harness.assertInGraveyard(player1, "Formless Genesis");
    }

    @Test
    @DisplayName("Can retrace again after resolving")
    void retracesRepeatedly() {
        FormlessGenesis genesis = new FormlessGenesis();
        harness.setGraveyard(player1, List.of(genesis));
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        Permanent firstToken = shapeshifterToken();
        int genesisIndex = gd.playerGraveyards.get(player1.getId()).indexOf(genesis);
        harness.castRetrace(player1, genesisIndex, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(1);
        Permanent secondToken = findPermanents(player1, "Shapeshifter").stream()
                .filter(token -> !token.getId().equals(firstToken.getId())).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, secondToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondToken)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Formless Genesis");
    }

    @Test
    @DisplayName("Retrace cannot discard a nonland card")
    void retraceRejectsNonlandDiscard() {
        harness.setGraveyard(player1, List.of(new FormlessGenesis()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Formless Genesis");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent shapeshifterToken() {
        return findPermanents(player1, "Shapeshifter").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
