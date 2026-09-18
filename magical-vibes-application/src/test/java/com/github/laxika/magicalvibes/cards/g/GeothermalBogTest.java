package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeothermalBog.class})
class GeothermalBogTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GeothermalBog()));

        harness.playLand(player1, 0);

        Permanent bog = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black or red mana")
    void manaAbilityAddsBlackOrRedMana() {
        Permanent bog = addReadyBog();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bog.isTapped()).isTrue();

        bog.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyBog() {
        Permanent bog = new Permanent(new GeothermalBog());
        bog.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bog);
        return bog;
    }
}
