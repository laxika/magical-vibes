package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KozileksTranslator.class)
class KozileksTranslatorTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 1 life produces one colorless mana")
    void paysLifeAndProducesColorlessMana() {
        Permanent translator = harness.addToBattlefieldAndReturn(player1, new KozileksTranslator());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(translator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void canBeActivatedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new KozileksTranslator());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
