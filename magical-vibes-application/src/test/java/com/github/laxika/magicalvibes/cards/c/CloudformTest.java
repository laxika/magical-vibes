package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudformTest extends BaseCardTest {

    @Test
    void manifestsTopCardAttachesAndGrantsFlyingAndHexproof() {
        Permanent manifested = resolveCloudform(new GrizzlyBears());
        Permanent cloudform = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isAura())
                .findFirst()
                .orElseThrow();

        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(cloudform.isAttached()).isTrue();
        assertThat(cloudform.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestedCreatureCanTurnFaceUpAndRetainsGrantedKeywords() {
        Permanent manifested = resolveCloudform(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void manifestedNoncreatureCannotTurnFaceUp() {
        Permanent manifested = resolveCloudform(new Forest());

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    private Permanent resolveCloudform(Card topCard) {
        harness.setHand(player1, List.of(new Cloudform()));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }
}
