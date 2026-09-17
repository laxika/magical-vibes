package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({ImaginaryFriends.class, GloriousAnthem.class})
class ImaginaryFriendsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 0/0 white Spirit tokens with flying")
    void createsThreeZeroZeroFlyingSpiritTokens() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        castImaginaryFriends();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(3);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCard().getPower()).isEqualTo(0);
            assertThat(spirit.getCard().getToughness()).isEqualTo(0);
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("The 0/0 Spirit tokens die to state-based actions without a boost")
    void zeroZeroSpiritTokensDieImmediately() {
        castImaginaryFriends();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void castImaginaryFriends() {
        harness.setHand(player1, List.of(new ImaginaryFriends()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
