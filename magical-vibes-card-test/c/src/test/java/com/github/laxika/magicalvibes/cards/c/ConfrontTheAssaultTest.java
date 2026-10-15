package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
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

@CardUsed({ConfrontTheAssault.class, SanctuaryCat.class})
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

    @Test
    @DisplayName("Cannot cast when your creature is attacking the other player")
    void cannotCastWhenAttackingOpponent() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SanctuaryCat());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        assertThatThrownBy(() -> harness.castFromHand(player1, new ConfrontTheAssault(), "{4}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast after the creature stops attacking")
    void cannotCastAfterAttackerLeavesCombat() {
        attackPlayer1();
        gd.playerBattlefields.get(player2.getId()).getFirst().setAttacking(false);

        assertThatThrownBy(() -> harness.castFromHand(player1, new ConfrontTheAssault(), "{4}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creates tokens even if the attacker leaves before resolution")
    void resolvesAfterAttackerLeavesBattlefield() {
        attackPlayer1();
        harness.castFromHand(player1, new ConfrontTheAssault(), "{4}{W}");
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        harness.assertInGraveyard(player1, "Confront the Assault");
    }

    private void castAndResolveConfrontTheAssault() {
        harness.castFromHand(player1, new ConfrontTheAssault(), "{4}{W}");
        harness.passBothPriorities();
    }

    private void addManaForConfrontTheAssault() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void attackPlayer1() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SanctuaryCat());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
    }
}
