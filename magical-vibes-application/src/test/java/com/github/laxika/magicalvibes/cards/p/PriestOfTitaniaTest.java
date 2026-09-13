package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishLyrist;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriestOfTitania.class, ElvishLyrist.class, GorillaWarrior.class})
class PriestOfTitaniaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one green mana for each Elf on the battlefield")
    void tappingAddsManaForElvesOnAllBattlefields() {
        Permanent priest = addCreatureReady(player1, new PriestOfTitania());
        harness.addToBattlefield(player1, new ElvishLyrist());
        harness.addToBattlefield(player2, new ElvishLyrist());
        harness.addToBattlefield(player2, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(priest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new PriestOfTitania());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
