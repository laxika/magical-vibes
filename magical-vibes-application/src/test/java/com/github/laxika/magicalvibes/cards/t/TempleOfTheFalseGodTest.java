package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempleOfTheFalseGod.class, Forest.class})
class TempleOfTheFalseGodTest extends BaseCardTest {

    @Test
    @DisplayName("Temple of the False God adds two colorless mana with five lands")
    void addsTwoColorlessManaWithFiveLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temple of the False God cannot activate with fewer than five lands")
    void cannotActivateWithFewerThanFiveLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addForests(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
