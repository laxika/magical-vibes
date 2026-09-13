package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConfrontTheAssault.class, GrizzlyBears.class})
class ConfrontTheAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Spirit tokens with flying when a creature attacks you")
    void createsThreeSpiritTokensWhenAttacked() {
        attackPlayer1();
        castAndResolveConfrontTheAssault();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(3).allSatisfy(spirit -> {
            assertThat(spirit.getCard().getPower()).isEqualTo(1);
            assertThat(spirit.getCard().getToughness()).isEqualTo(1);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Cannot cast when no creature is attacking you")
    void cannotCastWhenNotAttacked() {
        harness.setHand(player1, List.of(new ConfrontTheAssault()));
        addManaForConfrontTheAssault();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castAndResolveConfrontTheAssault() {
        harness.setHand(player1, List.of(new ConfrontTheAssault()));
        addManaForConfrontTheAssault();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void addManaForConfrontTheAssault() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void attackPlayer1() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
    }
}
