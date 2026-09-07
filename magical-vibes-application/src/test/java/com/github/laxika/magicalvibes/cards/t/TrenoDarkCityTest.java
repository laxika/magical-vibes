package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TrenoDarkCity.class)
class TrenoDarkCityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TrenoDarkCity()));

        harness.playLand(player1, 0);

        Permanent treno = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(treno.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds blue mana when blue is chosen")
    void manaAbilityAddsBlueMana() {
        Permanent treno = addReadyTreno();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(treno.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black mana when black is chosen")
    void manaAbilityAddsBlackMana() {
        Permanent treno = addReadyTreno();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(treno.isTapped()).isTrue();
    }

    private Permanent addReadyTreno() {
        Permanent treno = new Permanent(new TrenoDarkCity());
        treno.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(treno);
        return treno;
    }
}
