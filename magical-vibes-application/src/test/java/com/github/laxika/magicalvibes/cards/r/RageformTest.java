package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RageformTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card, attaches Rageform, and grants double strike")
    void manifestsTopCardAndAttaches() {
        Permanent manifested = resolveRageform(new GrizzlyBears());
        Permanent rageform = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isAura())
                .findFirst()
                .orElseThrow();

        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(rageform.isAttached()).isTrue();
        assertThat(rageform.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A manifested creature can turn face up and retains double strike")
    void manifestedCreatureTurnsFaceUpForManaCost() {
        Permanent manifested = resolveRageform(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A manifested noncreature cannot turn face up")
    void manifestedNoncreatureCannotTurnFaceUp() {
        Permanent manifested = resolveRageform(new Forest());

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    private Permanent resolveRageform(Card topCard) {
        harness.setHand(player1, List.of(new Rageform()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }
}
