package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LurkingEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Paying half your life makes Lurking Evil a 4/4 Phyrexian Horror with flying")
    void becomesCreatureAfterPayingHalfLife() {
        harness.setLife(player1, 20);
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, evil)).isTrue();
        assertThat(gqs.isEnchantment(gd, evil)).isFalse();
        assertThat(gqs.getEffectivePower(gd, evil)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, evil)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, evil))
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(gqs.hasKeyword(gd, evil, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Half your life rounds up for an odd life total")
    void halfLifeRoundsUp() {
        harness.setLife(player1, 21);
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, evil)).isTrue();
    }
}
