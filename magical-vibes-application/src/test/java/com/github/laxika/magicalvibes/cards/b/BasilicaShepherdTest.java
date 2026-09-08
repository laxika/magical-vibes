package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasilicaShepherdTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new BasilicaShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield creates two Phyrexian Mite tokens")
    void enteringBattlefieldCreatesTwoMites() {
        castAndResolve();

        List<Permanent> mites = findPermanents(player1, "Mite");
        assertThat(mites).hasSize(2).allSatisfy(mite -> {
            assertThat(mite.getCard().isToken()).isTrue();
            assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(mite.getCard().getColor()).isNull();
            assertThat(mite.getCard().getSubtypes())
                    .contains(CardSubtype.PHYREXIAN, CardSubtype.MITE);
            assertThat(mite.getCard().getKeywords()).contains(Keyword.TOXIC);
            assertThat(mite.getCard().getPower()).isEqualTo(1);
            assertThat(mite.getCard().getToughness()).isEqualTo(1);
            assertThat(bls.canBlock(gd, mite)).isFalse();
        });
    }

    @Test
    @DisplayName("A Mite dealing combat damage gives the damaged player a poison counter")
    void miteCombatDamageGivesPoisonCounter() {
        castAndResolve();

        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
