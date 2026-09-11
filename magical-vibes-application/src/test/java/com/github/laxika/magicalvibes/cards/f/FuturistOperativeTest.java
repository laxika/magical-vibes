package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FuturistOperative.class})
class FuturistOperativeTest extends BaseCardTest {

    @Test
    void tappedOperativeBecomesAnUnblockableOneOneHumanCitizen() {
        Permanent operative = addOperative();
        operative.tap();

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, operative))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
        assertThat(gqs.hasCantBeBlocked(gd, operative)).isTrue();
    }

    @Test
    void untapAbilityRemovesTappedCharacteristics() {
        Permanent operative = addOperative();
        operative.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(operative.isTapped()).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, operative))
                .doesNotContain(CardSubtype.CITIZEN);
        assertThat(gqs.hasCantBeBlocked(gd, operative)).isFalse();
    }

    private Permanent addOperative() {
        Permanent operative = new Permanent(new FuturistOperative());
        operative.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(operative);
        return operative;
    }
}
