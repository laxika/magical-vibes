package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FontOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("sacrificing Font of Vigor gains 7 life")
    void sacrificingFontOfVigorGainsSevenLife() {
        harness.setLife(player1, 10);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfVigor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == font.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }
}
