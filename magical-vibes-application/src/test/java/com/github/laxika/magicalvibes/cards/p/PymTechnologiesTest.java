package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PymTechnologies.class)
class PymTechnologiesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PymTechnologies()));

        harness.playLand(player1, 0);

        Permanent technologies = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(technologies.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds green mana when green is chosen")
    void manaAbilityAddsGreenMana() {
        Permanent technologies = addReadyTechnologies();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(technologies.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds blue mana when blue is chosen")
    void manaAbilityAddsBlueMana() {
        Permanent technologies = addReadyTechnologies();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(technologies.isTapped()).isTrue();
    }

    private Permanent addReadyTechnologies() {
        Permanent technologies = new Permanent(new PymTechnologies());
        technologies.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(technologies);
        return technologies;
    }
}
