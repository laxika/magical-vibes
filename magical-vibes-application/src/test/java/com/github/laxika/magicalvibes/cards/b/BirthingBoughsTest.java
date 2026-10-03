package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BirthingBoughs.class)
class BirthingBoughsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {4} and tapping Birthing Boughs creates a 2/2 colorless Shapeshifter with changeling")
    void createsChangelingToken() {
        harness.addToBattlefield(player1, new BirthingBoughs());
        Permanent boughs = findPermanent(player1, "Birthing Boughs");
        boughs.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(boughs.isTapped()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
    }

    @Test
    @DisplayName("A newly entered noncreature Birthing Boughs can activate immediately")
    void activatesImmediatelyAndUsesTheStack() {
        harness.addToBattlefield(player1, new BirthingBoughs());
        Permanent boughs = findPermanent(player1, "Birthing Boughs");
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(boughs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Shapeshifter")).isZero();
        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.FOREST)).isFalse();
    }

    @Test
    @DisplayName("Three mana cannot pay the four-mana activation cost")
    void insufficientManaDoesNotTapOrCreateAToken() {
        harness.addToBattlefield(player1, new BirthingBoughs());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(findPermanent(player1, "Birthing Boughs").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();
    }

    @Test
    @DisplayName("A tapped Birthing Boughs cannot activate even with sufficient mana")
    void tappedBoughsCannotActivate() {
        harness.addToBattlefield(player1, new BirthingBoughs());
        findPermanent(player1, "Birthing Boughs").setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();
    }
}
