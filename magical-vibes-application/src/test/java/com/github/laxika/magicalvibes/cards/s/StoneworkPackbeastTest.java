package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StoneworkPackbeast.class)
class StoneworkPackbeastTest extends BaseCardTest {

    @Test
    void isAlsoEachPartyCreatureType() {
        Permanent packbeast = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());

        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.CLERIC)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.ROGUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.WARRIOR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.WIZARD)).isTrue();
    }

    @Test
    void paysTwoManaForOneManaOfAnyColorWithoutTapping() {
        Permanent packbeast = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(packbeast.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
